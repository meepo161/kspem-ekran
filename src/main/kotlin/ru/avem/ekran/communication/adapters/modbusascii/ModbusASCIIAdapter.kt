package ru.avem.ekran.communication.adapters.modbusascii

import ru.avem.ekran.communication.Connection
import ru.avem.ekran.communication.adapters.Adapter
import ru.avem.ekran.communication.adapters.modbusascii.requests.*
import ru.avem.ekran.communication.adapters.utils.BitVector
import ru.avem.ekran.communication.adapters.utils.ModbusRegister

class ModbusASCIIAdapter(override val connection: Connection): Adapter {
    @ExperimentalUnsignedTypes
    fun readRegister(
        deviceId: Byte,
        registerId: Short,
        count: Int,
        customBaudrate: Int? = null
    ):
            List<ModbusRegister> {
        val request = ReadHoldingRegisters(
            deviceId = deviceId,
            registerId = registerId,
            count = count.toShort()
        )

        val response = doRequestForResponse(
            modbusAsciiRequest = request,
            customBaudrate = customBaudrate
        )
        return request.parseResponse(response)
    }


    private fun doRequestForResponse(
        modbusAsciiRequest: ModbusAsciiRequest,
        customBaudrate: Int?
    ): ByteArray {
        val response = ByteArray(modbusAsciiRequest.getResponseSize())
        connection.request(
            writeBuffer = modbusAsciiRequest.getRequestBytes(),
            readBuffer = response,
            customBaudrate = customBaudrate
        )

        return response
    }

    private fun readCoilStatus(
        deviceId: Byte,
        registerId: Short,
        count: Int,
        customBaudrate: Int?
    ): BitVector {
        val request = ReadCoilStatus(
            deviceId = deviceId,
            registerId = registerId,
            count = count.toShort()
        )
        val response = doRequestForResponse(
            modbusAsciiRequest = request,
            customBaudrate = customBaudrate
        )
        return request.parseResponse(response)
    }

    private fun readDiscreteInputs(
        deviceId: Int,
        registerId: Short,
        count: Int,
        customBaudrate: Int?
    ): BitVector {
        val request = ReadDiscreteInputs(
            deviceId = deviceId.toByte(),
            registerId = registerId,
            count = count.toShort()
        )

        val response = doRequestForResponse(
            modbusAsciiRequest = request,
            customBaudrate = customBaudrate
        )
        return request.parseResponse(response)
    }

    private fun readHoldingRegisters(
        deviceId: Byte,
        registerId: Short,
        count: Int,
        customBaudrate: Int? = null
    ): List<ModbusRegister> {
        val request = ReadHoldingRegisters(
            deviceId = deviceId,
            registerId = registerId,
            count = count.toShort()
        )

        val response = doRequestForResponse(
            modbusAsciiRequest = request,
            customBaudrate = customBaudrate
        )
        return request.parseResponse(response)
    }


    fun readInputRegisters(
        deviceId: Byte,
        registerId: Short,
        count: Int,
        customBaudrate: Int? = null
    ): List<ModbusRegister> {
        val request = ReadInputRegisters(
            deviceId = deviceId,
            registerId = registerId,
            count = count.toShort()
        )

        val response = doRequestForResponse(
            modbusAsciiRequest = request,
            customBaudrate = customBaudrate
        )
        return request.parseResponse(response)
    }


    // TODO coef go
    @ExperimentalUnsignedTypes
    fun writeRegister(
        deviceId: Byte,
        registerId: Short,
        register: ModbusRegister,
        customBaudrate: Int? = null
    ) {
        val request = PresetSingleRegister(
            deviceId = deviceId,
            registerId = registerId,
            registerData = register
        )

        val response = doRequestForResponse(
            modbusAsciiRequest = request,
            customBaudrate = customBaudrate
        )
        request.parseResponse(response)
    }
}

