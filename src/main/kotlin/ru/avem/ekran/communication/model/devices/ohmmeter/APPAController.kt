package ru.avem.ekran.communication.model.devices.ohmmeter

import ru.avem.ekran.communication.adapters.serial.SerialAdapter
import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceController
import ru.avem.ekran.communication.model.devices.ohmmeter.APPAModel.Companion.MODE_PARAM
import ru.avem.ekran.communication.model.devices.ohmmeter.APPAModel.Companion.RESISTANCE_PARAM
import ru.avem.ekran.communication.utils.TransportException
import ru.avem.ekran.utils.Log
import ru.avem.ekran.utils.sleep
import java.nio.ByteBuffer
import java.util.*

class APPAController(
    override val name: String,
    override val protocolAdapter: SerialAdapter,
    override val id: Byte
) : IDeviceController {
    val model = APPAModel()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()
    override val pollingMutex = Any()
    lateinit var response: APPAResponse

    companion object {
        const val NONE_MODE: Byte = 0
        const val L_MODE: Byte = 1
        const val C_MODE: Byte = 2
        const val R_MODE: Byte = 3
        const val DCR_MODE: Byte = 4 //TODO почитать какой регистр R L C, документацию
    }

    override fun readRegister(register: DeviceRegister) {
        transactionWithAttempts {
            val outputBuffer = ByteBuffer.allocate(5)
                .put(0x55.toByte())
                .put(0x55.toByte())
                .put(0x00.toByte())
                .put(0x00.toByte())
                .put(0xAA.toByte())
            var status = false
            protocolAdapter.write(outputBuffer.array())
            val inputArray = ByteArray(40)
            val finalBuffer = ByteBuffer.allocate(40)
            var attempt = 0
            do {
                sleep(2)
                val frameSize: Int = protocolAdapter.read(inputArray)
                if (frameSize != -1) {
                    finalBuffer.put(inputArray, 0, frameSize)
                }
            } while (finalBuffer.position() < 17 && ++attempt < 10)
            Log.i("TAG", "bytes: " + Arrays.toString(finalBuffer.array()))
            analyzeResponse(finalBuffer.array())
        }
    }

    private fun analyzeResponse(array: ByteArray) {
        val inputBuffer = ByteBuffer.allocate(17)
        (inputBuffer.clear() as ByteBuffer).put(array, 0, 17).flip().position(6)
        val mode = inputBuffer.get()
        val resistance = inputBuffer.short
        val ratio = getRatio(inputBuffer.get())
        val status = inputBuffer.get()

        response = APPAResponse(
            mode, if (status.toInt() and 0x1F == 3) {
                -2f
            } else {
                resistance * ratio
            }
        )
    }

    private fun getRatio(MScope: Byte): Float {
        var result = 1f
        when (MScope.toInt() and 7) {
            0 -> result /= 1f
            1 -> result /= 10f
            2 -> result /= 100f
            3 -> result /= 1000f
            4 -> result /= 10000f
        }
        when (MScope.toInt() and 0xF8 shr 3) {
            0 -> result *= 0f
            1 -> result *= 1f
            2 -> result *= 1000f
            3 -> result *= 1000000f
        }
        return result
    }

    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun <T : Number> writeRegister(register: DeviceRegister, value: T) {
    }

    override fun writeRegisters(register: DeviceRegister, values: List<Short>) {
    }

    override fun checkResponsibility() {
        try {
            model.registers.values.firstOrNull()?.let {
                readRegister(it)
            }
        } catch (ignored: TransportException) {
        }
    }

    override fun getRegisterById(idRegister: String) = model.getRegisterById(idRegister)


    fun getMode(): Byte {
        readRegister(getRegisterById(MODE_PARAM))
        return response.mode
    }

    fun getR(): Float {
        readRegister(getRegisterById(RESISTANCE_PARAM))
        return if (response.mode == R_MODE) {
            response.value
        } else {
            -1f
        }
    }

    fun getDCR(): Float {
        readRegister(getRegisterById(RESISTANCE_PARAM))
        return if (response.mode == DCR_MODE) {
            response.value
        } else {
            -1f
        }
    }

    fun getL(): Float {
        readRegister(getRegisterById(RESISTANCE_PARAM))
        return if (response.mode == L_MODE) {
            response.value
        } else {
            -1f
        }
    }

    fun getC(): Float {
        readRegister(getRegisterById(RESISTANCE_PARAM))
        return if (response.mode == C_MODE) {
            response.value
        } else {
            -1f
        }
    }
}

data class APPAResponse(val mode: Byte, val value: Float)
