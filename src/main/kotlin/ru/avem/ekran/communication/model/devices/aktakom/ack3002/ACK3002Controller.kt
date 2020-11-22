package ru.avem.ekran.communication.model.devices.aktakom.ack3002

import ru.avem.ekran.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.ekran.communication.adapters.utils.ModbusRegister
import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceController
import ru.avem.ekran.communication.utils.TransportException
import ru.avem.ekran.communication.utils.TypeByteOrder
import ru.avem.ekran.communication.utils.allocateOrderedByteBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.pow

class ACK3002Controller(
    override val name: String,
    override val protocolAdapter: ModbusRTUAdapter,
    override val id: Byte
) : IDeviceController {
    val model = ACK3002Model()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()
    override val pollingMutex = Any()

    companion object {
        const val TRIG_RESETER: Short = 0xFFFF.toShort()
        const val WD_RESETER: Short = 0b10
    }

    override fun readRegister(register: DeviceRegister) {
        transactionWithAttempts {
            val modbusRegister =
                protocolAdapter.readHoldingRegisters(id, register.address, 2).map(ModbusRegister::toShort)
            register.value = allocateOrderedByteBuffer(modbusRegister, TypeByteOrder.LITTLE_ENDIAN, 4).float.toDouble()
        }
    }

    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun <T : Number> writeRegister(register: DeviceRegister, value: T) {
        when (value) {
            is Float -> {
                val bb = ByteBuffer.allocate(4).putFloat(value).order(ByteOrder.LITTLE_ENDIAN)
                val registers = listOf(ModbusRegister(bb.getShort(2)), ModbusRegister(bb.getShort(0)))
                transactionWithAttempts {
                    protocolAdapter.presetMultipleRegisters(id, register.address, registers)
                }
            }
            is Int -> {
                val bb = ByteBuffer.allocate(4).putInt(value).order(ByteOrder.LITTLE_ENDIAN)
                val registers = listOf(ModbusRegister(bb.getShort(2)), ModbusRegister(bb.getShort(0)))
                transactionWithAttempts {
                    protocolAdapter.presetMultipleRegisters(id, register.address, registers)
                }
            }
            is Short -> {
                transactionWithAttempts {
                    protocolAdapter.presetSingleRegister(id, register.address, ModbusRegister(value))
                }
            }
            else -> {
                throw UnsupportedOperationException("Method can handle only with Float, Int and Short")
            }
        }
    }

    override fun writeRegisters(register: DeviceRegister, values: List<Short>) {
        val registers = values.map { ModbusRegister(it) }
        transactionWithAttempts {
            protocolAdapter.presetMultipleRegisters(id, register.address, registers)
        }
    }

    override fun checkResponsibility() {
        try {
            model.registers.values.firstOrNull()?.let {
                readRegister(it)
            }
        } catch (ignored: TransportException) {
        }
    }

    override fun getRegisterById(idRegister: String) = model.getRegisterById(idRegister)

    fun onBitInRegister(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        val mask = 2.0.pow(nor).toInt().toShort()
        model.outMask = model.outMask or mask

        writeRegister(register, model.outMask)
    }

    fun offBitInRegister(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        val mask = 2.0.pow(nor).toInt().inv()
        model.outMask = model.outMask and mask.toShort()

        writeRegister(register, model.outMask)
    }

    fun buildModule1Scheme() {
        // TODO: 18.09.2020 Implement
    }

    fun buildModule2Scheme() {
        // TODO: 18.09.2020 Implement
    }

    fun buildModule3Scheme() {
        // TODO: 18.09.2020 Implement
    }

    fun presetProtectionStates() {

    }

    fun resetTriggers() {
        with(getRegisterById(ACK3002Model.DI_01_16_RST)) {
            writeRegister(this, TRIG_RESETER)
        }
        with(getRegisterById(ACK3002Model.DI_17_32_RST)) {
            writeRegister(this, TRIG_RESETER)
        }
        with(getRegisterById(ACK3002Model.CMD)) {
            writeRegister(this, WD_RESETER)
        }
    }
}
