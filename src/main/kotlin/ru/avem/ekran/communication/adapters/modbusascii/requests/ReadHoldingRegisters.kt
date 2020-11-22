package ru.avem.ekran.communication.adapters.modbusascii.requests

import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_BYTE_COUNT
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_DEVICE_ID
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_END
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_FUNCTION
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_LRC
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_REGISTER
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_REGISTER_ID
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_REGISTER_ID_REQUEST_COUNT
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_REGISTER_ID_RESPONSE_COUNT
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_START
import ru.avem.ekran.communication.adapters.modbusascii.utils.CRC
import ru.avem.ekran.communication.adapters.modbusascii.utils.convertAsciiToHex
import ru.avem.ekran.communication.adapters.modbusascii.utils.convertToAsciiByteBuffer
import ru.avem.ekran.communication.adapters.utils.ModbusRegister
import java.nio.ByteBuffer


class ReadHoldingRegisters(override val deviceId: Byte, override val registerId: Short, val count: Short) :
    ModbusAsciiRequest {
    companion object {
        const val FUNCTION_CODE: Byte = 0x03
    }

    override val function: Byte = FUNCTION_CODE

    private val registerDataPosition = BYTE_SIZE_OF_START + (BYTE_SIZE_OF_DEVICE_ID + BYTE_SIZE_OF_FUNCTION + BYTE_SIZE_OF_REGISTER_ID_RESPONSE_COUNT) * 2


    private val dataLength = BYTE_SIZE_OF_DEVICE_ID +
            BYTE_SIZE_OF_FUNCTION +
            BYTE_SIZE_OF_REGISTER_ID +
            BYTE_SIZE_OF_REGISTER_ID_REQUEST_COUNT

    override fun getRequestBytes(): ByteArray {

        val data: ByteBuffer = ByteBuffer.allocate(dataLength)

        data.put(deviceId)
        data.put(function)
        data.putShort(registerId)
        data.putShort(count)

        val lrc = CRC.calc(data.array())
        data.flip()

        val asciiData = convertToAsciiByteBuffer(data)
        asciiData.flip()

        val result = ByteBuffer.allocate(getRequestSize())

        result.put(':'.toByte())
        result.put(asciiData)
        result.putShort(lrc)

        result.put('\r'.toByte())
        result.put('\n'.toByte())

        return result.array()
    }

    override fun getRequestSize() =
        BYTE_SIZE_OF_START +

                2 * (BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_REGISTER_ID +
                BYTE_SIZE_OF_REGISTER_ID_REQUEST_COUNT) +

                BYTE_SIZE_OF_LRC +
                BYTE_SIZE_OF_END

    override fun getResponseSize() =
        BYTE_SIZE_OF_START +
                2 * (BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_BYTE_COUNT +
                count * BYTE_SIZE_OF_REGISTER) +
                BYTE_SIZE_OF_LRC +
                BYTE_SIZE_OF_END

    fun parseResponse(response: ByteArray): List<ModbusRegister> {
        checkResponse(response)

        return List(count.toInt()) { index ->

            val registerData = ByteBuffer.allocate(BYTE_SIZE_OF_REGISTER * 2)

            registerData.put(response[registerDataPosition + index * BYTE_SIZE_OF_REGISTER + 0])
            registerData.put(response[registerDataPosition + index * BYTE_SIZE_OF_REGISTER + 1])
            registerData.put(response[registerDataPosition + index * BYTE_SIZE_OF_REGISTER + 2])
            registerData.put(response[registerDataPosition + index * BYTE_SIZE_OF_REGISTER + 3])

            val hexRegisterData: ByteBuffer = convertAsciiToHex(registerData)
            hexRegisterData.flip()

            ModbusRegister(hexRegisterData.short)
        }
    }
}
