package ru.avem.ekran.communication.adapters.modbusrtu.requests

import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_BYTE_COUNT
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_COIL_DATA_WORD
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_COIL_DATA_WORD_COUNT
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_CRC
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_DEVICE_ID
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_FUNCTION
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_REGISTER_ID
import ru.avem.ekran.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.REGISTER_ID_POSITION
import ru.avem.ekran.communication.adapters.modbusrtu.utils.CRC
import ru.avem.ekran.communication.adapters.utils.BitVector
import ru.avem.ekran.communication.utils.LogicException
import ru.avem.ekran.communication.utils.toShort
import java.nio.ByteBuffer

class ForceMultipleCoils(override val deviceId: Byte, override val registerId: Short, private val coils: BitVector) :
    ModbusRtuRequest {
    companion object {
        const val FUNCTION_CODE: Byte = 0x0F
    }

    override val function: Byte = FUNCTION_CODE
    private val coilDataCountPosition = REGISTER_ID_POSITION + BYTE_SIZE_OF_REGISTER_ID
    private val countOfCoilWords = coils.byteSize.toShort() // / BYTE_SIZE_OF_COIL_DATA_WORD).toShort() //TODO NEED TO BE CHECK WITH OWEN MYYYYYYY

    override fun getRequestBytes(): ByteArray = ByteBuffer.allocate(getRequestSize()).apply {
        put(deviceId)
        put(function)
        putShort(registerId)
        putShort(countOfCoilWords)
        put((countOfCoilWords * BYTE_SIZE_OF_COIL_DATA_WORD).toByte())
        coils.bytes.forEach {
            put(it)
        }
    }.also {
        CRC.sign(it)
    }.array()

    override fun getRequestSize() =
        BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_REGISTER_ID +
                BYTE_SIZE_OF_COIL_DATA_WORD_COUNT +
                BYTE_SIZE_OF_BYTE_COUNT +
                countOfCoilWords * BYTE_SIZE_OF_COIL_DATA_WORD +
                BYTE_SIZE_OF_CRC

    override fun getResponseSize() =
        BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_REGISTER_ID +
                BYTE_SIZE_OF_COIL_DATA_WORD_COUNT +
                BYTE_SIZE_OF_CRC

    fun parseResponse(response: ByteArray) {
        checkResponse(response)
        checkRegisterId((response[REGISTER_ID_POSITION + 0] to response[REGISTER_ID_POSITION + 1]).toShort())
        checkCountOfCoilWords((response[coilDataCountPosition + 0] to response[coilDataCountPosition + 1]).toShort())
    }

    private fun checkCountOfCoilWords(countCountOfCoilWordsFromResponse: Short) {
        if (countOfCoilWords != countCountOfCoilWordsFromResponse) {
            throw LogicException("Ошибка ответа: неправильный countOfCoilWords[$countCountOfCoilWordsFromResponse] вместо [$countOfCoilWords]")
        }
    }
}
