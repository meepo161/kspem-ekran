package ru.avem.ekran.communication.adapters.ack3002.driver

import ru.avem.ekran.communication.adapters.ack3002.driver.Utils.IAULNetListenerIACKScopeListener
import ru.avem.ekran.communication.adapters.ack3002.driver.Utils.TANetInterface
import ru.avem.ekran.communication.adapters.ack3002.driver.Utils.toHexString
import ru.avem.ekran.utils.Constants.Companion.OSCILLOSCOPE_DEVICE_NAME
import ru.avem.ekran.utils.sleep
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import javax.usb.*

class AULNetConnection(var commonInterfaces: IAULNetListenerIACKScopeListener) {
    companion object {
        const val REGISTER_FUNCTION = -37
        const val DATA_FUNCTION = -38
        const val CUBA_FUNCTION = -33
    }

    var cUBA: Byte = 0
    var connection: UsbInterface? = null
    var device: UsbDevice? = null
    var devname: String? = null
    var epRDST: UsbEndpoint? = null
    var epSNDCMD: UsbEndpoint? = null
    var epRDDT: UsbEndpoint? = null
    var epWRDT: UsbEndpoint? = null

    var interfaceMode = TANetInterface.aniAUN
    var outCmdCode: Byte = 0
    var supportedDevs = arrayOf("ACK-3102", "ACK-3002", "ACK-3712")
    val idEndPointRDST = 0
    val idEndPointSNDCM = 1
    val idEndPointRDDT = 2
    val idEndPointWRDT = 3
    val idEndPointCLOSE = 6

    init {
        device = findDevice()
        connect(isDeviceExists(), false)
    }

    fun isDeviceExists() = device != null

    private fun findDevice(): UsbDevice? {
        try {
            val allDevices: List<UsbDevice> = findAllDevices()
            var i = 0
            for (usbDevice in allDevices) {
                try {
                    i++
                    print(i.toString() + ". usbDeviceProductString: " + usbDevice.productString)
                    if (usbDevice.productString == OSCILLOSCOPE_DEVICE_NAME) {
                        return usbDevice
                    }
                } catch (ignored: UsbException) {
                } catch (ignored: UnsupportedEncodingException) {
                }
            }
        } catch (e: UsbException) {
            e.printStackTrace()
        }
        return null
    }

    private fun findAllDevices(): MutableList<UsbDevice> {
        val result: MutableList<UsbDevice> = ArrayList()
        val usbServices = UsbHostManager.getUsbServices()
        val rootUsbHub = usbServices.rootUsbHub
        findDriversInTheRoot(result, rootUsbHub)
        return result
    }

    private fun findDriversInTheRoot(result: MutableList<UsbDevice>, rootUsbHub: UsbHub) {
        val attachedUsbDevices: List<UsbDevice> = rootUsbHub.attachedUsbDevices as List<UsbDevice>
        for (usbDevice in attachedUsbDevices) {
            if (usbDevice.isUsbHub) {
                println(">--usbHub $usbDevice")
                findDriversInTheRoot(result, usbDevice as UsbHub)
            } else {
                println(">--usbDevice $usbDevice")
                val finalUsbDevice: UsbDevice? = probeDevice(usbDevice)
                println("finalUsbDevice $finalUsbDevice<--\n")
                if (finalUsbDevice != null) {
                    result.add(finalUsbDevice)
                }
            }
        }
    }

    private fun probeDevice(usbDevice: UsbDevice): UsbDevice? {
        try {
            println("probe usbDevice: $usbDevice")
            println("probe usbDeviceProductString: " + usbDevice.productString)
            return usbDevice
        } catch (ignored: UsbException) {
        } catch (ignored: UnsupportedEncodingException) {
        }
        return null
    }

    fun connect(isDeviceExist: Boolean, isNeedOnANConnect: Boolean): Boolean {
        var isDeviceConnected = false
        if (!isNeedOnANConnect) {
            if (isDeviceExist) {
                var bufWrittenLength = 0
                var writtenLength: Int
                connection = (device!!.activeUsbConfiguration.usbInterfaces[0] as UsbInterface).also {
                    it.claim()
                    val endpoints = it.usbEndpoints as List<UsbEndpoint?>
                    epSNDCMD =
                        endpoints.find { it?.usbEndpointDescriptor?.bEndpointAddress() == 1.toByte() } ?: error("Не найдена точка 1")
                    epRDST =
                        endpoints.find { it?.usbEndpointDescriptor?.bEndpointAddress() == 129.toByte() } ?: error("Не найдена точка 2")
                    epWRDT =
                        endpoints.find { it?.usbEndpointDescriptor?.bEndpointAddress() == 2.toByte() } ?: error("Не найдена точка 3")
                    epRDDT =
                        endpoints.find { it?.usbEndpointDescriptor?.bEndpointAddress() == 132.toByte() } ?: error("Не найдена точка 4")
                }

                val bufferPassword = ByteArray(16)
                writtenLength = bufWrittenLength
                if (bufWrittenLength >= 0) {
                    writtenLength = readRegister(221, bufferPassword)
                }
                devname = String(bufferPassword, 0, 8)

//            if (!isValidDevice(devname!!)) { TODO доделать
//                return connect(true)
//            }

                bufWrittenLength = writtenLength
                if (writtenLength >= 0) {
                    bufWrittenLength = readRegister(222, bufferPassword)
                }
                devname = devname + " #" + String(bufferPassword, 0, 16)
                if (bufWrittenLength >= 0) {
                    checkAUNVers()
                }
                if (bufWrittenLength >= 0) {
                    resetDev()
                }
                isDeviceConnected = true
            } else {
                AULNetTransfer(idEndPointCLOSE, null, 0)
                isDeviceConnected = false
            }
        } else {
            commonInterfaces.onANConnect(this)
        }
        return isDeviceConnected
    }

    private fun AULNetTransfer(idEndPoint: Int, buffer: ByteArray?, length: Int): Int {
        showEndpointById(idEndPoint);
        var writtenLength = 0
        if (connection != null) {
            val usbEndpoint: UsbEndpoint? = when (idEndPoint) {
                idEndPointRDST -> epRDST
                idEndPointSNDCM -> epSNDCMD
                idEndPointRDDT -> epRDDT
                idEndPointWRDT -> epWRDT
                else -> return length
            }
            val writtenLengthBulk = bulkTransfer(usbEndpoint, buffer, length)
            if (idEndPoint != idEndPointRDST) {
                writtenLength = writtenLengthBulk
                if (idEndPoint != idEndPointRDDT) {
                    return writtenLength
                }
            }
            writtenLength = writtenLengthBulk
        }
        return writtenLength
    }

    private fun showEndpointById(idEndPoint: Int) {
        when (idEndPoint) {
            0 -> println("idEndPointRDST")
            1 -> println("idEndPointSNDCM")
            2 -> println("idEndPointRDDT")
            3 -> println("idEndPointWRDT")
        }
    }

    @Synchronized private fun bulkTransfer(usbEndpoint: UsbEndpoint?, buffer: ByteArray?, length: Int): Int {
        var numBytesWritten = -1
        return if (usbEndpoint != null) {
            val usbPipe = usbEndpoint.usbPipe
            try {
                if (!usbPipe.isOpen) {
                    usbPipe.open()
                }
                val ib = if (usbEndpoint == epRDST || usbEndpoint == epRDDT) {
                    buffer
                } else {
                    buffer?.sliceArray(0 until length)
                }
                val usbIrp = usbPipe.asyncSubmit(ib)
                usbIrp.waitUntilComplete(100)
                numBytesWritten = usbIrp.actualLength
                println("${getCurrentTimeString()}: numBytesWritten(1) = $numBytesWritten")
                println("${getCurrentTimeString()}: toHexString(1) = ${buffer!!.toHexString(numBytesWritten)}")
                if ((usbEndpoint != epRDST) && (usbEndpoint != epRDDT) && numBytesWritten > 0) {
                    ib?.copyInto(buffer)
                }
            } catch (e: UsbException) {
                e.printStackTrace()
            }
            numBytesWritten
        } else {
            numBytesWritten
        }
    }

    fun closeEndPoints() {
        if (epRDST?.usbPipe?.isOpen == true) {
            epRDST?.usbPipe?.close()
        }
        if (epSNDCMD?.usbPipe?.isOpen == true) {
            epSNDCMD?.usbPipe?.close()
        }
        if (epRDDT?.usbPipe?.isOpen == true) {
            epRDDT?.usbPipe?.close()
        }
        if (epWRDT?.usbPipe?.isOpen == true) {
            epWRDT?.usbPipe?.close()
        }
    }

    private fun getCurrentTimeString() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())

    private fun checkAUNVers(): Boolean {
        var versionOk = false
        if (interfaceMode == TANetInterface.aniAUN || interfaceMode == TANetInterface.aniAUN2) {
            val buffer = ByteArray(16)
            selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND2)
            if (readRegister(42, buffer) < 0) {
                buffer[0] = 0
            }
            versionOk = buffer[0].toInt() == 1
        }
        if (versionOk) {
            interfaceMode = TANetInterface.aniAUN2
        }
        return versionOk
    }

    fun closeDevice() {
        AULNetTransfer(idEndPointCLOSE, null, 0)
    }

//    private fun isValidDevice(deviceName: String): Boolean {
//        var isValidDevice = false
//        for (supportedDev in supportedDevs) {
//            isValidDevice = isValidDevice or deviceName.startsWith(supportedDev)
//            if (isValidDevice) break
//        }
//        return isValidDevice
//    }

    fun readData(buffer: ByteArray, value: Byte): Int {
        var length: Short = 512
        val bufferCommand = ByteArray(512)
        if (interfaceMode != TANetInterface.aniAUN2) {
            length = 64
        }
        bufferCommand[0] = DATA_FUNCTION.toByte()
        bufferCommand[1] = value
        AULNetTransfer(idEndPointSNDCM, bufferCommand, 2)
        AULNetTransfer(idEndPointRDDT, bufferCommand, length.toInt())
        var lengthBuffer = -1
        var lengthRDDT: Int
        var lengthNeed = 0
        while (lengthNeed < buffer.size) {
            lengthRDDT = AULNetTransfer(idEndPointRDDT, bufferCommand, length.toInt())
            lengthBuffer = if (buffer.size - lengthNeed >= length) {
                length.toInt()
            } else {
                buffer.size - lengthNeed
            }
            System.arraycopy(bufferCommand, 0, buffer, lengthNeed, lengthBuffer)
            lengthBuffer = if (buffer.size - lengthNeed >= length) {
                length.toInt()
            } else {
                buffer.size - lengthNeed
            }
            lengthNeed += lengthBuffer
            lengthBuffer = lengthRDDT
        }
        return lengthBuffer
    }

    fun readRegister(command: Int, bufferRead: ByteArray?): Int {
        var length: Byte = 1
        val bufferCommand = ByteArray(16)
        if (221 == command) {
            bufferCommand[0] = -35
        } else if (222 == command) {
            bufferCommand[0] = -34
        } else {
            bufferCommand[0] = -36
            bufferCommand[1] = command.toByte()
            length = 2
            if (command shr 8 != 0) {
                bufferCommand[2] = (command shr 8).toByte()
                length = 3
                if (command shr 16 != 0) {
                    bufferCommand[3] = (command shr 16).toByte()
                    length = 4
                    if (command shr 24 != 0) {
                        bufferCommand[4] = (command shr 24).toByte()
                        length = 5
                    }
                }
            }
        }
        AULNetTransfer(idEndPointSNDCM, bufferCommand, length.toInt())
        AULNetTransfer(idEndPointRDST, bufferRead, 16)
        return AULNetTransfer(idEndPointRDST, bufferRead, 16)
    }

    private fun resetDev() {
        selectUBA(Constants.ACKScopeDrv.CUBA_COMMAND1)
        sleep(500)
    }

    fun selectUBA(cubaCommand: Byte): Int {
        val bufferCommand = ByteArray(16)
        bufferCommand[0] = CUBA_FUNCTION.toByte()
        bufferCommand[1] = cubaCommand
        cUBA = cubaCommand
        return AULNetTransfer(idEndPointSNDCM, bufferCommand, 2)
    }

    fun writeData(bufferWrite: ByteArray, value: Byte): Int {
        val bufferCommand = ByteArray(16)
        bufferCommand[0] = DATA_FUNCTION.toByte()
        bufferCommand[1] = value
        AULNetTransfer(idEndPointSNDCM, bufferCommand, 2)
        return AULNetTransfer(idEndPointWRDT, bufferWrite, bufferWrite.size)
    }

    fun writeRegister(command: Int, value: Byte): Int {
        val bufferCommand = ByteArray(16)
        bufferCommand[0] = REGISTER_FUNCTION.toByte()
        bufferCommand[1] = command.toByte()
        bufferCommand[2] = value
        return AULNetTransfer(idEndPointSNDCM, bufferCommand, 3)
    }

    fun writeRegister(command: Int, commandBytes: ByteArray): Int {
        val bufferCommand = ByteArray(16)
        bufferCommand[0] = REGISTER_FUNCTION.toByte()
        bufferCommand[1] = command.toByte()
        bufferCommand[2] = commandBytes[0]
        var length = 3
        if (command shr 8 != 0) {
            bufferCommand[3] = (command shr 8).toByte()
            bufferCommand[4] = commandBytes[1]
            length += 2
            if (command shr 16 != 0) {
                bufferCommand[5] = (command shr 16).toByte()
                bufferCommand[6] = commandBytes[2]
                length += 2
                if (command shr 24 != 0) {
                    bufferCommand[7] = (command shr 24).toByte()
                    bufferCommand[8] = commandBytes[3]
                    length += 2
                }
            }
        }
        return AULNetTransfer(idEndPointSNDCM, bufferCommand, length)
    }
}
