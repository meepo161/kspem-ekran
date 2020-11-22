package ru.avem.ekran.communication.adapters.ack3002.driver

import ru.avem.ekran.communication.adapters.ack3002.driver.Utils.IAULNetListenerIACKScopeListener
import ru.avem.ekran.communication.adapters.ack3002.driver.Utils.TANetInterface
import ru.avem.ekran.communication.adapters.ack3002.driver.Utils.limited
import ru.avem.ekran.communication.adapters.ack3002.driver.Utils.uByte
import ru.avem.ekran.utils.sleep
import java.util.concurrent.TimeUnit
import kotlin.experimental.and
import kotlin.experimental.or

class ACKScopeDrv(var commonInterfaces: IAULNetListenerIACKScopeListener) {
    companion object {
        val timebaseTab = doubleArrayOf(
            1.0E-8,
            2.0E-8,
            5.0E-8,
            1.0E-7,
            2.0E-7,
            5.0E-7,
            1.0E-6,
            2.0E-6,
            5.0E-6,
            1.0E-5,
            2.0E-5,
            5.0E-5,
            1.0E-4,
            2.0E-4,
            5.0E-4,
            0.001
        )
        val voltrangTab = doubleArrayOf(0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0)
    }

    private val clockSource: Int
    var connectState: Int
    private val coupling = IntArray(2)
    lateinit var data1: ByteArray
        private set
    lateinit var data2: ByteArray
        private set
    private val gainCode: Array<ByteArray> = arrayOf(
        byteArrayOf(-109, 51),
        byteArrayOf(-110, 50),
        byteArrayOf(-111, 49),
        byteArrayOf(-125, 35),
        byteArrayOf(-126, 34),
        byteArrayOf(-127, 33),
        byteArrayOf(3, 3),
        byteArrayOf(2, 2),
        byteArrayOf(1, 1),
        ByteArray(2)
    )
    private var generator: Int
    private var isLock = false
    var memorySize: Int
        private set
    private var needReset = 0
    private val offset = IntArray(2)
    var pAULNetConnection: AULNetConnection
    private var postTrgLength: Int
    private val probe = intArrayOf(1, 1)
    private val range = IntArray(2)
    var mRegStatus = 0
    var mSampleRate: Int
    private var isScroll: Boolean
    private var isStart: Boolean
    private var trgDelay: Int
    private val triggerLevel = IntArray(2)
    private var triggerLogic: Int
    var mTriggerMode: Int
    var mTriggerSource: Int

    init {
        for (channel in 0..1) {
            range[channel] = voltrangTab.size - 1
            coupling[channel] = 0
            offset[channel] = 2047
            triggerLevel[channel] = 2040
            probe[channel] = 1
        }
        triggerLogic = 0
        mTriggerMode = 0
        mTriggerSource = 0
        mSampleRate = 4
        clockSource = 0
        generator = 0
        isScroll = false
        isStart = false
        postTrgLength = 512
        trgDelay = 512
        memorySize = 65536
        pAULNetConnection = AULNetConnection(commonInterfaces)
        pAULNetConnection.interfaceMode = TANetInterface.aniAUN
        connectState = 1
    }

    private fun checkRegistrateState(var1: Double, var3: Double): Int {
        var var5: Byte = 0
        if (needReset and 1 != 0) {
            writeChannelControl(0)
        }
        if (needReset and 256 != 0) {
            writeChannelControl(1)
        }
        if (needReset and 2 != 0) {
            setOffset(offset[0], 0)
        }
        if (needReset and 512 != 0) {
            setOffset(offset[1], 1)
        }
        if (needReset and 4 != 0) {
            setTriggerLevel(triggerLevel[0], 0)
        }
        if (needReset and 1024 != 0) {
            setTriggerLevel(triggerLevel[1], 1)
        }
        if (needReset and 8 != 0) {
            setSampleRate(mSampleRate)
        }
        if (needReset and 8 != 0 || needReset and 4 != 0) {
            writeLowCmd()
        }
        if (0.001 * System.currentTimeMillis().toDouble() - var1 > var3) {
            var5 = 1
        }
        needReset = 0
        return var5.toInt()
    }

    private fun setNeedReset(var1: Int) {
        needReset = if (var1 != 0) {
            needReset or var1
        } else {
            0
        }
    }

    private fun setRegStatus(regStatus: Int) {
        if (this.mRegStatus != regStatus) {
            this.mRegStatus = regStatus
        }
    }

    private val channelsControl: Int
        get() {
            pAULNetConnection.selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND2)
            val buffer = ByteArray(16)
            pAULNetConnection.readRegister(4881, buffer)
            val leftUByte = uByte(buffer[0])
            val rightUByte = uByte(buffer[1])
            pAULNetConnection.javaClass
            pAULNetConnection.selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND1)
            return (leftUByte and 3 or (leftUByte and 4 shl 1) or (leftUByte and 8 shl 1) or (leftUByte and 16 shl 3) or (leftUByte and 32) or (leftUByte and 64 shr 4) xor 252 or (rightUByte and 3 or (rightUByte and 4 shl 1) or (rightUByte and 8 shl 1) or (rightUByte and 16 shl 1) or (rightUByte and 32 shl 1) or (rightUByte and 64 shr 4) xor 252) shl 8)
        }

    fun initConnection() {
        if (pAULNetConnection.isDeviceExists()) {
            pAULNetConnection.connect(isDeviceExist = true, isNeedOnANConnect = true)
        }
    }

    fun readMemorySize(): Int {
        val buffer = ByteArray(16)
        pAULNetConnection.readRegister(8, buffer)
        memorySize = when (buffer[0].toInt() and 15) {
            0 -> 65536
            1 -> 131072
            2 -> 262144
            3 -> 524288
            4 -> 1048576
            else -> 65536
        }
        return memorySize
    }

    fun writeChannelControl(var1: Int): Int {
        val var3 = gainCode[range[var1]][var1]
        var var2 = var3.toInt()
        if (2 == coupling[var1]) {
            val var6: Byte
            var6 = if (var1 != 0) {
                64
            } else {
                32
            }
            var2 = (var3 or var6).toInt()
        }
        var var8 = var2
        if (1 == coupling[var1]) {
            var8 = var2 or 8
        }
        var2 = var8
        if (3 == coupling[var1]) {
            var2 = var8 or 4
        }
        pAULNetConnection.selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND2)
        val var5: Byte = if (var1 == 0) {
            16
        } else {
            18
        }
        pAULNetConnection.writeRegister(var5.toInt(), (var2 xor 236).toByte())
        val var4 = pAULNetConnection
        pAULNetConnection.javaClass
        return var4.selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND1)
    }

    fun WriteChannelsControl(): Int {
        val var4: Byte = 0
        val channelsControlMask = channelsControl
        val var3: Byte = 0
        val var2 = gainCode[range[0]][0]
        var FF = var2.toInt()
        if (2 == coupling[0]) {
            FF = (var2 or 32).toInt()
        }
        var var8 = FF
        if (1 == coupling[0]) {
            var8 = FF or 8
        }
        FF = var8
        if (3 == coupling[0]) {
            FF = var8 or 4
        }
        val var6 = FF xor 236
        FF = var3.toInt()
        if (var6 != channelsControlMask and 255) {
            FF = 1
        }
        val var9 = gainCode[range[1]][1]
        var8 = var9.toInt()
        if (2 == coupling[1]) {
            var8 = (var9 or 64).toInt()
        }
        var var10 = var8
        if (1 == coupling[1]) {
            var10 = var8 or 8
        }
        var8 = var10
        if (3 == coupling[1]) {
            var8 = var10 or 4
        }
        var10 = var8 xor 236
        var8 = FF
        if (var10 != channelsControlMask and 255) {
            var8 = FF or 2
        }
        FF = var4.toInt()
        if (var8 != 0) {
            pAULNetConnection.selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND2)
            val var7 = ByteArray(16)
            var command: Short = 0
            when (var8) {
                1 -> {
                    command = 16
                    var7[0] = (var6 and 255).toByte()
                }
                2 -> {
                    command = 18
                    var7[0] = (var10 and 255).toByte()
                }
                3 -> {
                    command = 4624
                    var7[0] = (var6 and 255).toByte()
                    var7[1] = (var10 and 255).toByte()
                }
            }
            pAULNetConnection.writeRegister(command.toInt(), var7)
            val var12 = pAULNetConnection
            pAULNetConnection.javaClass
            FF = var12.selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND1)
        }
        return FF
    }

    fun writeLowCmd(): Int {
        val var3: Byte = if (generator != 0) {
            32
        } else {
            0
        }
        var var2 = var3
        var var1 = var2
        if (isScroll) {
            if (isStart) {
                var1 = (var2 or 16) as Byte
            }
        }
        var2 = var1
        if (triggerLogic == 1) {
            var2 = (var1 or 8) as Byte
        }
        var1 = if (mTriggerMode == 0) {
            (var2 or 6) as Byte
        } else {
            when (mTriggerSource) {
                1 -> (var2 or 2) as Byte
                2, 3 -> var2
                4 -> (var2 or 4) as Byte
                else -> var2
            }
        }
        var2 = var1
        if (isStart) {
            if (!isScroll) {
                var2 = (var1 or 1) as Byte
            }
        }
        var1 = var2
        if (clockSource == 1) {
            var1 = (var2 or 64) as Byte
        }
        return pAULNetConnection.writeRegister(12, var1)
    }

    fun ackReset() {
        setTrgDelay(trgDelay)
        setPostTrgLength(postTrgLength)
        setOffset(offset[0], 0)
        setOffset(offset[1], 1)
        setTriggerLevel(triggerLevel[0], 0)
        setTriggerLevel(triggerLevel[1], 1)
        WriteChannelsControl()
        setSampleRate(mSampleRate)
        writeLowCmd()
    }

    fun getProbe(var1: Int): Int {
        return probe[var1]
    }

    fun getRange(var1: Int): Int {
        return range[var1]
    }

    val readAddress: Int
        get() {
            val buffer = ByteArray(16)
            pAULNetConnection.readRegister(459522, buffer)
            return (uByte(buffer[0]) + (uByte(buffer[1]) shl 8) + (uByte(buffer[2]) shl 16)) % memorySize
        }

    fun getSampleRate(): Int {
        return mSampleRate
    }

    val status: Byte
        get() {
            val buffer = ByteArray(16)
            pAULNetConnection.readRegister(4, buffer)
            return buffer[0]
        }

    fun getTriggerMode(): Int {
        return mTriggerMode
    }

    fun getTriggerSource(): Int {
        return mTriggerSource
    }

    val writeAddress: Int
        get() {
            val buffer = ByteArray(16)
            pAULNetConnection.readRegister(393472, buffer)
            return (uByte(buffer[0]) + (uByte(buffer[1]) shl 8) + (uByte(buffer[2]) shl 16)) % memorySize
        }

    fun readRAM(var1: Int, var2: Int): Int {
        if (var1 == 0) {
            data1 = ByteArray(var2)
            pAULNetConnection.readData(data1, (-113).toByte())
        } else {
            data2 = ByteArray(var2)
            pAULNetConnection.readData(data2, (-97).toByte())
        }
        return 0
    }

    fun readWaveform(): Int {
        Thread { ReadWaveformTask().doInBackground(this) }.start()
        return 0
    }

    fun setCoupling(value: Int, var2: Int) {
        var value = value
        value = limited(0, value, 3)
        coupling[var2] = value
        if (isLock) {
            setNeedReset(1 shl var2 * 8)
        } else {
            writeChannelControl(var2)
        }
    }

    fun setGenerator(_var1: Int) {
        var var1 = _var1
        var1 = limited(0, var1, 2)
        generator = var1
        val var3 = mSampleRate.toByte()
        var var2 = var3
        if (2 == var1) {
            var2 = (var3 or 16) as Byte
        }
        if (isLock) {
            setNeedReset(8)
        } else {
            pAULNetConnection.writeRegister(11, var2)
            writeLowCmd()
        }
    }

    fun setOffset(var1: Int, var2: Int) {
        val var4 = limited(0, var1, 4095)
        offset[var2] = var4
        if (isLock) {
            setNeedReset(2 shl var2 * 8)
        } else {
            pAULNetConnection.selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND2)
            var var6 = if (var2 == 0) {
                21
            } else {
                25
            }
            var var7 = var6
            var var3 = (4095 - var4 shr 8 and 255).toByte()
            pAULNetConnection.writeRegister(var7, var3)
            var6 = if (var2 == 0) {
                20
            } else {
                24
            }
            var7 = var6
            var3 = (4095 - var4 and 255).toByte()
            pAULNetConnection.writeRegister(var7, var3)
            val aulNetConnection = pAULNetConnection
            pAULNetConnection.javaClass
            aulNetConnection.selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND1)
        }
    }

    fun setPostTrgLength(var1: Int) {
        var var1 = var1
        var1 = limited(2, var1, memorySize)
        postTrgLength = var1
        val var2 = memorySize - (var1 - 2)
        var1 = var2
        if (var2 != 0) {
            var1 = var2 - 1
        }
        setReadAddress(var1)
        readAddress
    }

    fun setProbe(var1: Int, var2: Int) {
        var var3 = var1
        if (var1 <= 0) {
            var3 = 1
        }
        probe[var2] = var3
    }

    fun setRange(indexNewValue: Int, channel: Int) {
        var index = indexNewValue
        index = limited(0, index, voltrangTab.size - 1)
        range[channel] = index
        if (isLock) {
            setNeedReset(1 shl channel * 8)
        } else {
            writeChannelControl(channel)
        }
    }

    fun setReadAddress(var1: Int): Int {
        val var2 = ByteArray(16)
        var2[2] = (var1 shr 16 and 255).toByte()
        var2[1] = (var1 shr 8 and 255).toByte()
        var2[0] = (var1 and 255).toByte()
        return pAULNetConnection.writeRegister(459522, var2)
    }

    private fun setSampleRate(_var1: Int) {
        var var1 = _var1
        val var2 = limited(0, var1, timebaseTab.size - 1)
        mSampleRate = var2
        var1 = var2
        if (2 == generator) {
            var1 = var2 or 16
        }
        if (isLock) {
            setNeedReset(8)
        } else {
            pAULNetConnection.writeRegister(11, var1.toByte())
        }
    }

    fun setScroll(var1: Boolean) {
        isScroll = var1
        if (isLock) {
            setNeedReset(4)
        } else {
            writeLowCmd()
        }
    }

    fun setStart(var1: Boolean) {
        isStart = var1
        if (isLock) {
            setNeedReset(4)
        } else {
            writeLowCmd()
        }
    }

    fun setTrgDelay(_var1: Int) {
        var var1 = _var1
        var1 = limited(0, var1, memorySize)
        trgDelay = var1
        val var2 = memorySize - var1
        var1 = var2
        if (var2 != 0) {
            var1 = var2 - 1
        }
        setWriteAddress(var1)
    }

    fun setTriggerLevel(var1: Int, var2: Int) {
        val var3 = limited(0, var1, 4095)
        triggerLevel[var2] = var3
        if (isLock) {
            setNeedReset(4 shl var2 * 8)
        } else {
            pAULNetConnection.selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND2)
            val var5: Short
            var5 = if (var2 == 0) {
                5655
            } else {
                6683
            }
            val var4 = ByteArray(16)
            var4[0] = (var3 shr 8 and 255).toByte()
            var4[1] = (var3 and 255).toByte()
            pAULNetConnection.writeRegister(var5.toInt(), var4)
            val var6 = pAULNetConnection
            pAULNetConnection.javaClass
            var6.selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND1)
        }
    }

    fun setTriggerLogic(var1: Int) {
        triggerLogic = limited(0, var1, 2)
        if (isLock) {
            setNeedReset(4)
        } else {
            writeLowCmd()
        }
    }

    fun setTriggerMode(var1: Int) {
        mTriggerMode = limited(0, var1, 3)
        if (isLock) {
            setNeedReset(4)
        } else {
            writeLowCmd()
        }
    }

    fun setTriggerSource(var1: Int) {
        mTriggerSource = limited(0, var1, 1)
        if (isLock) {
            setNeedReset(4)
        } else {
            writeLowCmd()
        }
    }

    fun setWriteAddress(var1: Int): Int {
        val var2 = ByteArray(16)
        var2[2] = (var1 shr 16 and 255).toByte()
        var2[1] = (var1 shr 8 and 255).toByte()
        var2[0] = (var1 and 255).toByte()
        return pAULNetConnection.writeRegister(393472, var2)
    }

    fun startNormal() {
        isStart = true
        val var1 = mTriggerMode
        mTriggerMode = 1
        writeLowCmd()
        mTriggerMode = var1
    }

    interface IACKScopeListener {
        fun onDataReady(ackScopeDrv: ACKScopeDrv?)
        fun onRegStatusChange(ackScopeDrv: ACKScopeDrv?)
    }

    private inner class ReadWaveformTask {
        fun doInBackground(vararg ackScopeDrvs: ACKScopeDrv): Int {
            val lengthDelay = ackScopeDrvs[0].postTrgLength + ackScopeDrvs[0].trgDelay
            ackScopeDrvs[0].isStart = false
            ackScopeDrvs[0].ackReset()
            ackScopeDrvs[0].startNormal()
            ackScopeDrvs[0].setRegStatus(1)
            var currentTimeMillis = System.currentTimeMillis().toDouble()
            var cuba1: Byte
            do {
                try {
                    TimeUnit.MILLISECONDS.sleep(50L)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                ackScopeDrvs[0].isLock = true
                val status = ackScopeDrvs[0].status
                ackScopeDrvs[0].isLock = false
                cuba1 = (status and 14) as Byte
            } while (checkRegistrateState(
                    0.001 * currentTimeMillis, 0.2
                ) == 0 && (cuba1.toInt() and 2) == 0 && cuba1.toInt() != 0
            )
            var _currentTimeMicros = 0.001 * System.currentTimeMillis().toDouble()
            val sampleRate = timebaseTab[ackScopeDrvs[0].mSampleRate]
            val _yRate = (ackScopeDrvs[0].trgDelay + 100).toDouble() * sampleRate
            var _newYRate = _yRate
            var isTriggerModeOn = true
            var cuba2 = cuba1
            currentTimeMillis = _currentTimeMicros
            if (_yRate < 0.2) {
                _newYRate = 0.2
                currentTimeMillis = _currentTimeMicros
            }
            var cuba3: Byte
            do {
                sleep(50L)
                cuba3 = pAULNetConnection.cUBA
                pAULNetConnection.javaClass
                if (cuba3.toInt() != 1) {
                    cuba3 = cuba2
                } else {
                    ackScopeDrvs[0].isLock = true
                    cuba3 = ackScopeDrvs[0].status
                    ackScopeDrvs[0].isLock = false
                    cuba1 = (cuba3 and 14)
                    if (cuba1.toInt() and 8 != 0) {
                        ackScopeDrvs[0].setRegStatus(2)
                    }
                    var localTriggerMode = isTriggerModeOn
                    _currentTimeMicros = currentTimeMillis
                    if (isTriggerModeOn && ackScopeDrvs[0].mTriggerMode == 0 && cuba1.toInt() and 4 != 0 && cuba1.toInt() and 8 == 0 && checkRegistrateState(
                            currentTimeMillis, _newYRate
                        ) != 0
                    ) {
                        val ackScopeDrv = ackScopeDrvs[0]
                        localTriggerMode = false
                        ackScopeDrv.setTriggerMode(0)
                        _currentTimeMicros = 0.001 * System.currentTimeMillis().toDouble()
                    }
                    isTriggerModeOn = localTriggerMode
                    cuba3 = cuba1
                    currentTimeMillis = _currentTimeMicros
                    if (!localTriggerMode) {
                        if (checkRegistrateState(_currentTimeMicros, (lengthDelay * 2).toDouble() * sampleRate) != 0) {
                            cuba3 = 0
                            currentTimeMillis = _currentTimeMicros
                        }
                    }
                }
                cuba2 = cuba3
            } while (cuba3.toInt() and 6 != 0)
            ackScopeDrvs[0].setStart(false)
            ackScopeDrvs[0].isLock = true
            ackScopeDrvs[0].setRegStatus(3)
            val _lastAddress = ackScopeDrvs[0].writeAddress - lengthDelay
            var startAddress = _lastAddress
            if (_lastAddress < 0) {
                startAddress = _lastAddress + ackScopeDrvs[0].memorySize
            }
            ackScopeDrvs[0].setReadAddress(startAddress)
            ackScopeDrvs[0].readAddress
            ackScopeDrvs[0].readRAM(0, lengthDelay)
            ackScopeDrvs[0].setReadAddress(startAddress)
            startAddress = ackScopeDrvs[0].readRAM(1, lengthDelay)
            ackScopeDrvs[0].isLock = false
            ackScopeDrvs[0].setRegStatus(0)
            (commonInterfaces as IACKScopeListener).onDataReady(ackScopeDrvs[0])
            return startAddress
        }
    }

    fun closeEndPoints() {
        pAULNetConnection.closeEndPoints()
    }
}
