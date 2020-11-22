package ru.avem.ekran.communication.adapters.modbusrtu.requests

import ru.avem.ekran.communication.adapters.modbusrtu.utils.CRC
import ru.avem.ekran.communication.utils.LogicException
import ru.avem.ekran.communication.utils.TransportException
import kotlin.experimental.or

interface ModbusRtuRequest {
    val deviceId: Byte
    val function: Byte
    val registerId: Short

    fun getRequestBytes(): ByteArray

    fun getRequestSize(): Int
    fun getResponseSize(): Int

    fun checkResponse(response: ByteArray) {
        checkResponseSize(response.size)
        checkCRC(response)
        checkDeviceId(response[DEVICE_ID_POSITION])

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

    fun checkDeviceId(deviceIdFromResponse: Byte) {
        if (deviceId != deviceIdFromResponse) {
            throw TransportException("Ошибка ответа: неправильный id устройства $deviceIdFromResponse")
        }
    }

    fun checkRegisterId(registerIdFromResponse: Short) {
        if (registerId != registerIdFromResponse) {
            throw LogicException("Ошибка ответа: неправильный registerId[$registerIdFromResponse] вместо [$registerId]")
        }
    }

    fun checkFunctionSame(response: ByteArray) {
        if (!(function == response[FUNCTION_POSITION] || function == (response[FUNCTION_POSITION] or 0x80.toByte()))) {
            throw TransportException("Ошибка ответа: неправильная функция")
        }
    }

    fun checkFunctionIsError(response: ByteArray) {
        if (function == (response[FUNCTION_POSITION] or 0x80.toByte())) {
            when (response[ERROR_POSITION]) {
                0x01.toByte() -> throw LogicException("Ошибка устройства: Принятый код функции не может быть обработан.")
                0x02.toByte() -> throw LogicException("Ошибка устройства: Адрес данных, указанный в запросе, недоступен.")
                0x03.toByte() -> throw LogicException("Ошибка устройства: Значение, содержащееся в поле данных запроса, является недопустимой величиной.")
                0x04.toByte() -> throw LogicException("Ошибка устройства: Невосстанавливаемая ошибка имела место, пока ведомое устройство пыталось выполнить затребованное действие.")
                0x05.toByte() -> throw LogicException("Ошибка устройства: Ведомое устройство приняло запрос и обрабатывает его, но это требует много времени. Этот ответ предохраняет ведущее устройство от генерации ошибки тайм-аута.")
                0x06.toByte() -> throw LogicException("Ошибка устройства: Ведомое устройство занято обработкой команды. Ведущее устройство должно повторить сообщение позже, когда ведомое освободится.")
                0x07.toByte() -> throw LogicException("Ошибка устройства: Ведомое устройство не может выполнить программную функцию, заданную в запросе. Этот код возвращается для неуспешного программного запроса, использующего функции с номерами 13 или 14. Ведущее устройство должно запросить диагностическую информацию или информацию об ошибках от ведомого.")
                0x08.toByte() -> throw LogicException("Ошибка устройства: Ведомое устройство при чтении расширенной памяти обнаружило ошибку контроля четности. Главный может повторить запрос позже, но обычно в таких случаях требуется ремонт оборудования.")

                else -> throw LogicException("Ошибка устройства: Неизвестная ошибка [${response[ERROR_POSITION]}]")
            }
        }
    }

    companion object {
        const val BYTE_SIZE_OF_DEVICE_ID: Int = 1
        const val BYTE_SIZE_OF_FUNCTION: Int = 1
        const val BYTE_SIZE_OF_REGISTER_ID: Int = 2
        const val BYTE_SIZE_OF_REGISTER_ID_COUNT: Int = 2
        const val BYTE_SIZE_OF_BIT_COUNT_POSITION: Int = 1

        const val BYTE_SIZE_OF_BYTE_COUNT: Int = 1
        const val BYTE_SIZE_OF_CRC: Int = 2

        const val BYTE_SIZE_OF_REGISTER: Int = 2
        const val BYTE_SIZE_OF_REGISTER_COUNT: Int = 2
        const val BYTE_SIZE_OF_COIL_DATA_WORD: Int = 2
        const val BYTE_SIZE_OF_COIL_DATA_WORD_COUNT: Int = 2

        const val DEVICE_ID_POSITION = 0
        const val FUNCTION_POSITION = DEVICE_ID_POSITION + BYTE_SIZE_OF_DEVICE_ID
        const val ERROR_POSITION = FUNCTION_POSITION + BYTE_SIZE_OF_FUNCTION
        const val REGISTER_ID_POSITION = FUNCTION_POSITION + BYTE_SIZE_OF_FUNCTION
        const val REGISTER_ID_COUNT_POSITION = FUNCTION_POSITION + BYTE_SIZE_OF_FUNCTION
        const val BIT_COUNT_POSITION = FUNCTION_POSITION + BYTE_SIZE_OF_FUNCTION
    }
}
