package ru.avem.ekran.communication

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import mu.KotlinLogging
import ru.avem.ekran.app.Ekran.Companion.isAppRunning
import ru.avem.ekran.communication.utils.SerialParameters
import ru.avem.ekran.communication.utils.TransportException
import ru.avem.ekran.communication.utils.toHexString
import java.lang.Thread.sleep
import java.lang.Thread.yield
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

class PortController(
    private val adapterName: String,
    private val serialParameters: SerialParameters,
    private val timeoutRead: Int,
    private val timeoutWrite: Int
) {
    private val logger = KotlinLogging.logger("Port [$adapterName]")

    private var portFailureCause = "Не определено"

    private val locker = Any()

    private var port: SerialPort? = null

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isNeedConnecting = false

    @Volatile
    private var isTryingToConnect = false

    init {
        thread {
            while (isAppRunning) {
                if (isNeedConnecting) {
                    isTryingToConnect = true
                    if (!isConnecting()) {
                        isInitialized = try {
                            init()
                        } catch (e: Exception) {
                            false
                        }
                    }
                    isTryingToConnect = false
                    sleep(1)
                } else {
                    sleep(100)
                }
            }
        }
    }

    fun connect() {
        isNeedConnecting = true
        while (!isTryingToConnect) {
            sleep(10)
        }
        while (isTryingToConnect) {
            yield()
        }
    }

    fun isConnecting() = isInitialized && (port?.isOpen ?: false)

    private fun init() = try {
        port = findPort()

        port?.setComPortParameters(
            serialParameters.baudrate,
            serialParameters.dataBits,
            serialParameters.stopBits,
            serialParameters.parity
        )
        port?.setComPortTimeouts(
            SerialPort.TIMEOUT_WRITE_BLOCKING or SerialPort.TIMEOUT_READ_BLOCKING,
            timeoutRead,
            timeoutWrite
        )
        port?.openPort()

        port?.isOpen ?: false
    } catch (e: SerialPortInvalidPortException) {
        portFailureCause = e.message ?: "Не определено"
        false
    }

    private fun findPort(): SerialPort {
        val foundCommPorts = SerialPort.getCommPorts().filter { it.portDescription == adapterName }

        when {
            foundCommPorts.isNullOrEmpty() -> throw SerialPortInvalidPortException("Порт с именем $adapterName не найден")
            foundCommPorts.size > 1 -> throw SerialPortInvalidPortException("Портов с именем $adapterName больше одного")
            else -> return foundCommPorts.first()
        }
    }

    fun write(buffer: ByteArray, size: Long, repeatAttempt: Int = 1): Int {
        if (repeatAttempt > 1) {
            readRubbish("before write for $repeatAttempt attempt")
        }

        val writtenBytesCount = port?.writeBytes(buffer, size)
        logger.info { ">>> [${buffer.toHexString(prefix = "", infix = " ")}] as $writtenBytesCount bytes" }
        return writtenBytesCount ?: -1
    }

    fun write(request: String) {
        val requestByte = request.toByteArray(StandardCharsets.US_ASCII)
        port?.writeBytes(requestByte, requestByte.size.toLong())
    }


    fun read(buffer: ByteArray, size: Long, repeatAttempt: Int = 1): Int {
        val needReadAdditional = (port?.bytesAvailable()?.toLong() ?: size) > size

        val readBytesCount = port?.readBytes(buffer, size)
        logger.info("<<< [${buffer.toHexString(prefix = "", infix = " ")}] as $readBytesCount bytes")

        if (needReadAdditional || repeatAttempt > 1) {
            readRubbish("after read for $repeatAttempt attempt")
        }

        return readBytesCount ?: -1
    }

    fun read(request: String): Int {
        val requestByte = request.toByteArray(StandardCharsets.US_ASCII)
        port?.writeBytes(requestByte, requestByte.size.toLong())

        val inputBytes = ByteArray(255)
        val readBytes = port?.readBytes(inputBytes, inputBytes.size.toLong())
        return readBytes ?: -1
    }

    private fun readRubbish(message: String) {
        val rubbishSize = 256
        val rubbish = ByteArray(rubbishSize)
        val readRubbishCount = port?.readBytes(rubbish, rubbishSize.toLong())

        readRubbishCount?.let {
            if (it > 0) {
                logger.info(
                    "<<< $message read/clear rubbish[${
                        rubbish.copyOfRange(0, it).toHexString(prefix = "", infix = " ")
                    }] as $it bytes"
                )
            }
        }
    }

    fun request(
        writeBuffer: ByteArray,
        readBuffer: ByteArray,
        frameBetweenTimeout: Long = 8,
        frameAfterTimeout: Long = 8,
        repeatAttempt: Int = 1,
        customBaudrate: Int? = serialParameters.baudrate
    ) {
        synchronized(locker) {
            port?.let {
                try {
                    if (customBaudrate != null && customBaudrate != serialParameters.baudrate) {
                        port?.baudRate = customBaudrate
                    }
                    val writtenBytesCount = write(writeBuffer, writeBuffer.size.toLong(), repeatAttempt)

                    if (writtenBytesCount > 0) {
                        if (frameBetweenTimeout > 0) {
                            sleep(frameBetweenTimeout)
                        }
                        read(readBuffer, readBuffer.size.toLong(), repeatAttempt)
                    }

                    if (frameAfterTimeout > 0) {
                        sleep(frameAfterTimeout)
                    }
                } catch (e: Exception) {
                    throw e
                } finally {
                    if (customBaudrate != null && customBaudrate != serialParameters.baudrate) {
                        port?.baudRate = serialParameters.baudrate
                    }
                }
            } ?: throw TransportException("Соединение с портом невозможно. Причина: $portFailureCause")
        }
    }

    fun disconnect() {
        isNeedConnecting = false
        while (isTryingToConnect) {
            sleep(10)
        }
        if (isConnecting()) {
            port?.closePort()
        }
    }
}
