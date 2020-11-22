package ru.avem.ekran.communication.adapters.modbusascii.requests

import ru.avem.ekran.communication.adapters.modbusascii.utils.CRC
import ru.avem.ekran.communication.adapters.modbusascii.utils.getHexString
import ru.avem.ekran.communication.adapters.modbusascii.utils.getShortHexString
import ru.avem.ekran.communication.utils.LogicException
import ru.avem.ekran.communication.utils.TransportException

interface ModbusAsciiRequest {
    val deviceId: Byte
    val function: Byte
    val registerId: Short

    fun getRequestBytes(): ByteArray

    fun getRequestSize(): Int
    fun getResponseSize(): Int

    fun checkResponse(response: ByteArray) {
        checkResponseSize(response.size)

        checkCRC(response)

        checkDeviceId(response)

        checkFunctionSame(response)
        checkFunctionIsError(response)
    }

    fun checkResponseSize(size: Int) {
        if (getResponseSize() != size) {
            throw TransportException("Ошибка ответа: неправильный размер")
        }
    }

    fun checkCRC(response: ByteArray) {
        if (!CRC.isValid(response)) {
            throw TransportException("Ошибка ответа: неправильный CRC")
        }
    }

    fun checkDeviceId(response: ByteArray) {

        val deviceIdFromResponse = getShortHexString(response, DEVICE_ID_POSITION)

        if (getHexString(deviceId) != deviceIdFromResponse) {
            throw TransportException("Ошибка ответа: неправильный id устройства $deviceIdFromResponse")
        }
    }

    fun checkRegisterId(response: ByteArray) {
        val registerIdFromResponse = getShortHexString(response, DEVICE_ID_POSITION + (BYTE_SIZE_OF_DEVICE_ID + BYTE_SIZE_OF_FUNCTION) * 2)

        if (getHexString(registerId) != registerIdFromResponse) {
            throw LogicException("Ошибка ответа: неправильный registerId[$registerIdFromResponse] вместо [$registerId]")
        }
    }

    fun checkFunctionSame(response: ByteArray) {
        val functionIdFromResponse = getShortHexString(response, DEVICE_ID_POSITION + BYTE_SIZE_OF_DEVICE_ID * 2)
        val functionIdFromResponseWithoutError = "0" + functionIdFromResponse[1]

        if (!(getHexString(function) == functionIdFromResponse || getHexString(function) == functionIdFromResponseWithoutError)) {
            throw TransportException("Ошибка ответа: неправильная функция")
        }
    }

    fun checkFunctionIsError(response: ByteArray) {
        val functionIdFromResponse = getShortHexString(response, DEVICE_ID_POSITION + (BYTE_SIZE_OF_DEVICE_ID + BYTE_SIZE_OF_FUNCTION) * 2)
        val functionIdFromResponseWithoutError = "0" + functionIdFromResponse[1]

        if (getHexString(function) == functionIdFromResponseWithoutError) {
            when (functionIdFromResponseWithoutError) {
                "01" -> throw LogicException("Ошибка устройства: Принятый код функции не может быть обработан.")
                "02" -> throw LogicException("Ошибка устройства: Адрес данных, указанный в запросе, недоступен.")
                "03" -> throw LogicException("Ошибка устройства: Значение, содержащееся в поле данных запроса, является недопустимой величиной.")
                "04" -> throw LogicException("Ошибка устройства: Невосстанавливаемая ошибка имела место, пока ведомое устройство пыталось выполнить затребованное действие.")
                "05" -> throw LogicException("Ошибка устройства: Ведомое устройство приняло запрос и обрабатывает его, но это требует много времени. Этот ответ предохраняет ведущее устройство от генерации ошибки тайм-аута.")
                "06" -> throw LogicException("Ошибка устройства: Ведомое устройство занято обработкой команды. Ведущее устройство должно повторить сообщение позже, когда ведомое освободится.")
                "07" -> throw LogicException("Ошибка устройства: Ведомое устройство не может выполнить программную функцию, заданную в запросе. Этот код возвращается для неуспешного программного запроса, использующего функции с номерами 13 или 14. Ведущее устройство должно запросить диагностическую информацию или информацию об ошибках от ведомого.")
                "08" -> throw LogicException("Ошибка устройства: Ведомое устройство при чтении расширенной памяти обнаружило ошибку контроля четности. Главный может повторить запрос позже, но обычно в таких случаях требуется ремонт оборудования.")

                else -> throw LogicException("Ошибка устройства: Неизвестная ошибка [${functionIdFromResponseWithoutError}]")
            }
        }
    }

    companion object {

        val START_PACKET_BYTES: ByteArray = byteArrayOf(0x3A)
        val END_PACKET_BYTES: ByteArray = byteArrayOf(0x0D, 0x0A)

        const val BYTE_SIZE_OF_START: Int = 1
        const val BYTE_SIZE_OF_END: Int = 2
        const val BYTE_SIZE_OF_DEVICE_ID: Int = 1
        const val BYTE_SIZE_OF_FUNCTION: Int = 1

        const val BYTE_SIZE_OF_REGISTER_ID: Int = 2

        const val BYTE_SIZE_OF_REGISTER_ID_REQUEST_COUNT: Int = 2
        const val BYTE_SIZE_OF_REGISTER_ID_RESPONSE_COUNT: Int = 1
        const val BYTE_SIZE_OF_BIT_COUNT_POSITION: Int = 1

        const val BYTE_SIZE_OF_BYTE_COUNT: Int = 1

        const val BYTE_SIZE_OF_LRC: Int = 2

        const val BYTE_SIZE_OF_REGISTER: Int = 2

        const val BYTE_SIZE_OF_REGISTER_COUNT: Int = 2
        const val BYTE_SIZE_OF_COIL_DATA_WORD: Int = 2
        const val BYTE_SIZE_OF_COIL_DATA_WORD_COUNT: Int = 2

        const val DEVICE_ID_POSITION = BYTE_SIZE_OF_START
        const val FUNCTION_POSITION = DEVICE_ID_POSITION + BYTE_SIZE_OF_DEVICE_ID

        const val REGISTER_ID_COUNT_POSITION = FUNCTION_POSITION + BYTE_SIZE_OF_FUNCTION
        const val ERROR_POSITION = FUNCTION_POSITION + BYTE_SIZE_OF_FUNCTION

        const val BIT_COUNT_POSITION = FUNCTION_POSITION + BYTE_SIZE_OF_FUNCTION


        const val REGISTER_ID_POSITION = FUNCTION_POSITION + BYTE_SIZE_OF_FUNCTION

        const val BYTE_SIZE_OF_CRC: Int = 2
    }
}
