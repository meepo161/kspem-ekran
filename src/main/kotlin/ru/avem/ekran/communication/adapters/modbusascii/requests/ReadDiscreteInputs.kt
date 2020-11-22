package ru.avem.ekran.communication.adapters.modbusascii.requests

import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BIT_COUNT_POSITION
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_BIT_COUNT_POSITION
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_BYTE_COUNT
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_CRC
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_DEVICE_ID
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_FUNCTION
import ru.avem.ekran.communication.adapters.modbusascii.requests.ModbusAsciiRequest.Companion.BYTE_SIZE_OF_REGISTER_ID
import ru.avem.ekran.communication.adapters.utils.BitVector
import java.nio.ByteBuffer
import kotlin.math.ceil

class ReadDiscreteInputs(override val deviceId: Byte, override val registerId: Short, val count: Short) :
    ModbusAsciiRequest {
    companion object {
        const val FUNCTION_CODE: Byte = 0x02
    }

    override val function: Byte = FUNCTION_CODE
    private val coilDataPosition = BIT_COUNT_POSITION + BYTE_SIZE_OF_BIT_COUNT_POSITION

    override fun getRequestBytes(): ByteArray = ByteBuffer.allocate(getRequestSize()).apply {
        put(deviceId)
        put(function)
        putShort(registerId)
        putShort(count)
    }.also {
        // CRC.sign(it)
    }.array()

    override fun getRequestSize() =
        BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_REGISTER_ID +
                // BYTE_SIZE_OF_REGISTER_ID_COUNT +
                BYTE_SIZE_OF_CRC

    override fun getResponseSize() =
        BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_BYTE_COUNT +
                ceil(count / 8.0).toInt() +
                BYTE_SIZE_OF_CRC

    fun parseResponse(response: ByteArray): BitVector {
        checkResponse(response)

        return BitVector.createBitVector(response.copyOfRange(coilDataPosition, coilDataPosition + count), size = count.toInt())
    }
}
