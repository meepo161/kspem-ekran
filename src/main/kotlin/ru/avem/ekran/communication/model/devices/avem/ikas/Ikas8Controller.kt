package ru.avem.ekran.communication.model.devices.avem.ikas

import ru.avem.ekran.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.ekran.communication.adapters.utils.ModbusRegister
import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceController
import ru.avem.ekran.communication.model.devices.avem.ikas.Ikas8Model.Companion.AA
import ru.avem.ekran.communication.model.devices.avem.ikas.Ikas8Model.Companion.AB
import ru.avem.ekran.communication.model.devices.avem.ikas.Ikas8Model.Companion.AC
import ru.avem.ekran.communication.model.devices.avem.ikas.Ikas8Model.Companion.BB
import ru.avem.ekran.communication.model.devices.avem.ikas.Ikas8Model.Companion.BC
import ru.avem.ekran.communication.model.devices.avem.ikas.Ikas8Model.Companion.CC
import ru.avem.ekran.communication.model.devices.avem.ikas.Ikas8Model.Companion.CFG_SCHEME
import ru.avem.ekran.communication.model.devices.avem.ikas.Ikas8Model.Companion.SERIAL
import ru.avem.ekran.communication.model.devices.avem.ikas.Ikas8Model.Companion.START_STOP
import ru.avem.ekran.communication.utils.TransportException
import ru.avem.ekran.communication.utils.TypeByteOrder
import ru.avem.ekran.communication.utils.allocateOrderedByteBuffer
import ru.avem.ekran.utils.sleep
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Ikas8Controller(
    override val name: String, override val protocolAdapter: ModbusRTUAdapter, override val id: Byte
) : IDeviceController {
    private val model = Ikas8Model()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val pollingMutex = Any()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()

    override fun readRegister(register: DeviceRegister) {
        transactionWithAttempts {
            when (register.valueType) {
                DeviceRegister.RegisterValueType.SHORT -> {
                    val value = protocolAdapter.readHoldingRegisters(id, register.address, 1).first().toShort()
                    register.value = value
                }
                DeviceRegister.RegisterValueType.FLOAT -> {
                    val modbusRegister =
                        protocolAdapter.readHoldingRegisters(id, register.address, 4).map(ModbusRegister::toShort)
                    register.value = allocateOrderedByteBuffer(modbusRegister, TypeByteOrder.BIG_ENDIAN, 4).float
                }
                DeviceRegister.RegisterValueType.INT32 -> {
                    val modbusRegister =
                        protocolAdapter.readHoldingRegisters(id, register.address, 4).map(ModbusRegister::toShort)
                    register.value = allocateOrderedByteBuffer(modbusRegister, TypeByteOrder.BIG_ENDIAN, 4).int
                }
            }
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
                val bb = ByteBuffer.allocate(4).putInt(value).order(ByteOrder.BIG_ENDIAN)
                val registers = listOf(ModbusRegister(bb.getShort(0)), ModbusRegister(bb.getShort(2)))
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

    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun writeRegisters(register: DeviceRegister, values: List<Short>) {
        val registers = values.map { ModbusRegister(it) }
        transactionWithAttempts {
            protocolAdapter.presetMultipleRegisters(id, register.address, registers)
        }
    }

    override fun getRegisterById(idRegister: String) = model.getRegisterById(idRegister)

    override fun checkResponsibility() {
        try {
            model.registers.values.firstOrNull()?.let {
                readRegister(it)
            }
        } catch (ignored: TransportException) {
        }
    }

    fun startMeasuringAB() {
        writeRegister(getRegisterById(START_STOP), 0x00)
        sleep(1000)
        writeRegister(getRegisterById(CFG_SCHEME), AB)
        sleep(1000)
        writeRegister(getRegisterById(START_STOP), 0x01)
        sleep(2000)
    }

    fun startMeasuringBC() {
        writeRegister(getRegisterById(START_STOP), 0x00)
        sleep(1000)
        writeRegister(getRegisterById(CFG_SCHEME), BC)
        sleep(1000)
        writeRegister(getRegisterById(START_STOP), 0x01)
        sleep(2000)
    }

    fun startMeasuringAC() {
        writeRegister(getRegisterById(START_STOP), 0x00)
        sleep(1000)
        writeRegister(getRegisterById(CFG_SCHEME), AC)
        sleep(1000)
        writeRegister(getRegisterById(START_STOP), 0x01)
        sleep(2000)
    }

    fun startMeasuringAA() {
        writeRegister(getRegisterById(START_STOP), 0x00)
        sleep(1000)
        writeRegister(getRegisterById(CFG_SCHEME), AA)
        sleep(1000)
        writeRegister(getRegisterById(START_STOP), 0x01)
        sleep(2000)
    }

    fun startMeasuringBB() {
        writeRegister(getRegisterById(START_STOP), 0x00)
        sleep(1000)
        writeRegister(getRegisterById(CFG_SCHEME), BB)
        sleep(1000)
        writeRegister(getRegisterById(START_STOP), 0x01)
        sleep(2000)
    }

    fun startMeasuringCC() {
        writeRegister(getRegisterById(START_STOP), 0x00)
        sleep(1000)
        writeRegister(getRegisterById(CFG_SCHEME), CC)
        sleep(1000)
        writeRegister(getRegisterById(START_STOP), 0x01)
        sleep(2000)
    }

    fun startSerialMeasuring() {
        writeRegister(getRegisterById(SERIAL), 0x01)
        sleep(1000)
        writeRegister(getRegisterById(START_STOP), 0x01)
        sleep(2000)
    }

    fun stopSerialMeasuring() {
        writeRegister(getRegisterById(SERIAL), 0x00)
        sleep(1000)
    }
}
