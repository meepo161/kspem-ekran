package ru.avem.ekran.communication

import ru.avem.ekran.communication.utils.SerialParameters

data class Connection(
    val adapterName: String,
    val serialParameters: SerialParameters,
    val timeoutRead: Int,
    val timeoutWrite: Int,
    val attemptCount: Int = 5,
    var frameBetweenTimeout: Long = 8,
    var frameAfterTimeout: Long = 8
) {
    private val portController = PortController(adapterName, serialParameters, timeoutRead, timeoutWrite)

    fun connect() = portController.connect()

    fun isConnecting() = portController.isConnecting()

    fun write(buffer: ByteArray, size: Long = buffer.size.toLong()) = portController.write(buffer, size)
    fun read(buffer: ByteArray, size: Long = buffer.size.toLong()) = portController.read(buffer, size)

    fun write(request: String) = portController.write(request)
    fun read(request: String) = portController.read(request)

    fun request(
        writeBuffer: ByteArray,
        readBuffer: ByteArray,
        frameBetweenTimeout: Long = this.frameBetweenTimeout,
        frameAfterTimeout: Long = this.frameAfterTimeout,
        customBaudrate: Int? = serialParameters.baudrate
    ) = portController.request(
        writeBuffer = writeBuffer,
        readBuffer = readBuffer,
        frameBetweenTimeout = frameBetweenTimeout,
        frameAfterTimeout = frameAfterTimeout,
        customBaudrate = customBaudrate
    )

    fun disconnect() = portController.disconnect()
}
