package ru.avem.ekran.communication.model.devices.bris.m4122

import com.fazecast.jSerialComm.SerialPortIOException
import org.slf4j.LoggerFactory
import ru.avem.ekran.communication.adapters.serial.SerialAdapter
import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceController
import ru.avem.ekran.communication.model.devices.ohmmeter.APPAResponse
import ru.avem.ekran.communication.utils.TransportException
import ru.avem.ekran.communication.utils.toHexString
import java.lang.Thread.sleep
import java.nio.ByteBuffer
import java.nio.ByteOrder

class M4122Controller(
    override val name: String,
    override val protocolAdapter: SerialAdapter,
    override val id: Byte
) : IDeviceController {
    val model = M4122Model()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()
    override val pollingMutex = Any()
    lateinit var response: APPAResponse

    companion object {
        const val PRODUCT_NAME = "CP2102 USB to UART M4122"
        private val logger = LoggerFactory.getLogger(this::class.java)
        private val WATCHDOG_REQ = byteArrayOf(0x55)
        private val EMPTY_WATCHDOG = byteArrayOf(0x00)
        private val LOCKER = Any()
    }

    enum class MeasuringType(var cmd: Byte) {
        VOLTAGE(0x57),
        RESISTANCE(0x58),
        ABSORPTION(0x59),
        POLARIZATION(0x5A)
    }

    fun resetWatchdog() {
        logger.debug("M4122 Reset Watchdog: ${WATCHDOG_REQ.toHexString()}")
        val buffer = ByteArray(1)
        synchronized(LOCKER) {
            protocolAdapter.write(WATCHDOG_REQ/*, WATCHDOG_REQ.size.toLong()*/)
            sleep(500)
            isResponding = protocolAdapter.read(buffer /*, 1*/) != -1 && !buffer.contentEquals(EMPTY_WATCHDOG)
            logger.debug("M4122 is responding?: $isResponding")
        }
        logger.debug("M4122 Watchdog: ${buffer.toHexString()}")
    }

    fun setVoltageAndStartMeasuring(voltage: Short = -1, type: MeasuringType): Int {
        synchronized(LOCKER) {
            if (voltage == (-1).toShort()) {
                sendRequest(byteArrayOf(0x55, type.cmd), responseSize = 1)
            } else {
                sendRequest(byteArrayOf(0x55, 0x6B, *convertToBytes(voltage), type.cmd), responseSize = 1)
            }

            when (type) {
                MeasuringType.RESISTANCE -> loop@ while (true) {
                    val response = sendRequest(byteArrayOf(0x55), responseSize = 2)
                    when {
                        0x67 in response -> {
                            return -2 //TODO возможно обрыв
                        }
                        0x5B in response -> {
                            val values = sendRequest(byteArrayOf(0x5C, 0x55), responseSize = 12)
                            sleep(50)
                            val byteArray = ByteArray(255)
                            if (protocolAdapter.read(byteArray/*, byteArray.size.toLong()*/) == -1) {
                                throw SerialPortIOException("Error while read data from port $PRODUCT_NAME")
                            }
                            val indexOf5E = values.indexOf(0x5E)
                            val measuringBytes = values.copyOfRange(indexOf5E - 4, indexOf5E)
                            return (ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(measuringBytes)
                                .flip() as ByteBuffer).int
                        }
                    }
                }
                else -> {
                    return -1
                }
            }
        }
    }

    private fun convertToBytes(voltage: Short) =
        ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(voltage).array()

    private fun sendRequest(request: ByteArray, sleepMills: Long = 200, responseSize: Int = 255): ByteArray {
        if (protocolAdapter.write(request /*, request.size.toLong()*/) == -1) {
            throw SerialPortIOException("Error while write data to port $PRODUCT_NAME")
        }
        sleep(sleepMills)
        val response = ByteArray(responseSize)
        if (protocolAdapter.read(response/*, response.size.toLong()*/) == -1) {
            throw SerialPortIOException("Error while read data from port $PRODUCT_NAME")
        }
        return response
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

}
