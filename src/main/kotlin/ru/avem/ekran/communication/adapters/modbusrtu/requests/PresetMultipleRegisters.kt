package ru.avem.ekran.communication.adapters.modbusrtu.requests

import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_BYTE_COUNT
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_CRC
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_DEVICE_ID
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_FUNCTION
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_REGISTER
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_REGISTER_COUNT
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_REGISTER_ID
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.REGISTER_ID_POSITION
import ru.avem.ekran.communication.adapters.modbusrtu.utils.CRC
import ru.avem.ekran.communication.adapters.utils.ModbusRegister
import ru.avem.ekran.communication.utils.LogicException
import ru.avem.ekran.communication.utils.toShort
import java.nio.ByteBuffer

class PresetMultipleRegisters(override val deviceId: Byte, override val registerId: Short, val registers: List<ModbusRegister>) :
    ModbusRtuRequest {
    companion object {
        const val FUNCTION_CODE: Byte = 0x10
    }

    override val function: Byte = FUNCTION_CODE
    private val registerDataCountPosition = REGISTER_ID_POSITION + BYTE_SIZE_OF_REGISTER_ID

    val count = registers.size.toShort()

    override fun getRequestBytes(): ByteArray = ByteBuffer.allocate(getRequestSize()).apply {
        put(deviceId)
        put(function)
        putShort(registerId)
        putShort(count)
        put((count * BYTE_SIZE_OF_REGISTER).toByte())
        registers.forEach {
            putShort(it.toShort())
        }
    }.also {
        CRC.sign(it)
    }.array()

    override fun getRequestSize() =
        BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_REGISTER_ID +
                BYTE_SIZE_OF_REGISTER_COUNT +
                BYTE_SIZE_OF_BYTE_COUNT +
                BYTE_SIZE_OF_REGISTER * count +
                BYTE_SIZE_OF_CRC

    override fun getResponseSize() =
        BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_REGISTER_ID +
                BYTE_SIZE_OF_REGISTER_COUNT +
                BYTE_SIZE_OF_CRC

    fun parseResponse(response: ByteArray) {
        checkResponse(response)
        checkRegisterId((response[REGISTER_ID_POSITION + 0] to response[REGISTER_ID_POSITION + 1]).toShort())
        checkCount((response[registerDataCountPosition + 0] to response[registerDataCountPosition + 1]).toShort())
    }

    private fun checkCount(countFromResponse: Short) {
        if (count != countFromResponse) {
            throw LogicException("Ошибка ответа: неправильный count[$countFromResponse] вместо [$count]")
        }
    }
}
