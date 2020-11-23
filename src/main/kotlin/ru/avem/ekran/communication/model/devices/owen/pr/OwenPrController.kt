package ru.avem.ekran.communication.model.devices.owen.pr

import ru.avem.ekran.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.ekran.communication.adapters.utils.ModbusRegister
import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceController
import ru.avem.ekran.communication.utils.TransportException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.pow

class OwenPrController(
    override val name: String,
    override val protocolAdapter: ModbusRTUAdapter,
    override val id: Byte
) : IDeviceController {
    val model = OwenPrModel()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()
    override val pollingMutex = Any()

    var outMask: Short = 0
    var outMask2: Short = 0

    override fun readRegister(register: DeviceRegister) {
        isResponding = try {
            transactionWithAttempts {
                val modbusRegister =
                    protocolAdapter.readHoldingRegisters(id, register.address, 1).map(ModbusRegister::toShort)
                register.value = modbusRegister.first()
            }
            true
        } catch (e: TransportException) {
            false
        }
    }


    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun <T : Number> writeRegister(register: DeviceRegister, value: T) {
        isResponding = try {
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
                        protocolAdapter.presetMultipleRegisters(id, register.address, listOf(ModbusRegister(value)))
                    }
                }
                else -> {
                    throw UnsupportedOperationException("Method can handle only with Float, Int and Short")
                }
            }
            true
        } catch (e: TransportException) {
            false
        }
    }

    override fun writeRegisters(register: DeviceRegister, values: List<Short>) {
        val registers = values.map { ModbusRegister(it) }
        isResponding = try {
            transactionWithAttempts {
                protocolAdapter.presetMultipleRegisters(id, register.address, registers)
            }
            true
        } catch (e: TransportException) {
            false
        }
    }

    override fun checkResponsibility() {
        model.registers.values.firstOrNull()?.let {
            readRegister(it)
        }
    }

    override fun getRegisterById(idRegister: String) = model.getRegisterById(idRegister)

    private fun onBitInRegister(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask = outMask or 2.0.pow(nor).toInt().toShort()
        writeRegister(register, outMask)
    }

    private fun onBitInRegister2(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask2 = outMask2 or 2.0.pow(nor).toInt().toShort()
        writeRegister(register, outMask2)
    }

    private fun offBitInRegister(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask = outMask and 2.0.pow(nor).toInt().inv().toShort()
        writeRegister(register, outMask)
    }

    private fun offBitInRegister2(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask2 = outMask2 and 2.0.pow(nor).toInt().inv().toShort()
        writeRegister(register, outMask2)
    }


    fun initOwenPR() {
        writeRegister(getRegisterById(OwenPrModel.RESET_TIMER), 0)
        writeRegister(getRegisterById(OwenPrModel.RESET_TIMER), 1)
        writeRegister(getRegisterById(OwenPrModel.RES_REGISTER), 1)
    }

    fun resetKMS() {
        writeRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 0)
        writeRegister(getRegisterById(OwenPrModel.KMS2_REGISTER), 0)
    }

    fun onKM51() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 1)
    }

    fun onKM53() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 2)
    }

    fun onKM54() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 3)
    }

    fun onKM52() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 4)
    }

    fun onKM11() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 5)
    }

    fun onKM12() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 6)
    }

    fun onSound() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 7)
    }

    fun onLight() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 8)
    }

    fun onKM1() {
        onBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 1)
    }

    fun onKM32() {
        onBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 2)
    }

    fun onKM31() {
        onBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 3)
    }

    fun onKM42() {
        onBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 4)
    }

    fun onKM41() {
        onBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 5)
    }

    fun onKM33() {
        onBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 6)
    }

    fun onKM44() {
        onBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 7)
    }

    fun onKM30() {
        onBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 8)
    }

    fun offKM51() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 1)
    }

    fun offKM53() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 2)
    }

    fun offKM54() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 3)
    }

    fun offKM52() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 4)
    }

    fun offKM11() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 5)
    }

    fun offKM12() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 6)
    }

    fun offSound() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 7)
    }

    fun offLight() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 8)
    }

    fun offKM1() {
        offBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 1)
    }

    fun offKM32() {
        offBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 2)
    }

    fun offKM31() {
        offBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 3)
    }

    fun offKM42() {
        offBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 4)
    }

    fun offKM41() {
        offBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 5)
    }

    fun offKM33() {
        offBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 6)
    }

    fun offKM44() {
        offBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 7)
    }

    fun offKM30() {
        offBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 8)
    }

    fun offAllKMs() {
        writeRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 0)
        writeRegister(getRegisterById(OwenPrModel.KMS2_REGISTER), 0)
        outMask = 0
        outMask2 = 0
    }
}
