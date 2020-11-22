package ru.avem.ekran.communication.adapters.modbusrtu.requests

import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_BIT_COUNT_POSITION
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_BYTE_COUNT
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_CRC
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_DEVICE_ID
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_FUNCTION
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_REGISTER
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_REGISTER_ID
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_REGISTER_ID_COUNT
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.REGISTER_ID_COUNT_POSITION
import ru.avem.ekran.communication.adapters.modbusrtu.utils.CRC
import ru.avem.ekran.communication.adapters.utils.ModbusRegister
import java.nio.ByteBuffer

class ReadInputRegisters(override val deviceId: Byte, override val registerId: Short, val count: Short) :
    ModbusRtuRequest {
    companion object {
        const val FUNCTION_CODE: Byte = 0x04
    }

    override val function: Byte = FUNCTION_CODE
    private val registerDataPosition = REGISTER_ID_COUNT_POSITION + BYTE_SIZE_OF_BIT_COUNT_POSITION

    override fun getRequestBytes(): ByteArray = ByteBuffer.allocate(getRequestSize()).apply {
        put(deviceId)
        put(function)
        putShort(registerId)
        putShort(count)
    }.also {
        CRC.sign(it)
    }.array()

    override fun getRequestSize() =
        BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_REGISTER_ID +
                BYTE_SIZE_OF_REGISTER_ID_COUNT +
                BYTE_SIZE_OF_CRC

    override fun getResponseSize() =
        BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_BYTE_COUNT +
                count * BYTE_SIZE_OF_REGISTER +
                BYTE_SIZE_OF_CRC

    fun parseResponse(response: ByteArray): List<ModbusRegister> {
        checkResponse(response)

        return List(count.toInt()) { index ->
            ModbusRegister(
                b1 = response[registerDataPosition + index * BYTE_SIZE_OF_REGISTER + 0],
                b2 = response[registerDataPosition + index * BYTE_SIZE_OF_REGISTER + 1]
            )
        }

    }
}
