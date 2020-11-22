package ru.avem.ekran.communication.model.devices.idc

import ru.avem.ekran.communication.adapters.stringascii.StringASCIIAdapter
import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceController
import ru.avem.ekran.communication.utils.TransportException

class IDCController(
    override val name: String,
    override val protocolAdapter: StringASCIIAdapter,
    override val id: Byte
) : IDeviceController {
    val model = IDCModel()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()
    override val pollingMutex = Any()

    companion object {
    }


    override fun readRequest(request: String): Int {
       return protocolAdapter.read(id, request)
    }

    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun writeRequest(request: String) {
        protocolAdapter.write(id, request)
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

    fun remoteControl() {
        writeRequest("SYSTem:REMote")
    }

    fun localControl() {
        writeRequest("SYSTem:LOCal")
    }

    fun setVoltage(voltage: Double) {
        writeRequest("SOUR:VOLT $voltage")
    }

    fun onVoltage() {
        writeRequest("OUTP ON")
    }

    fun offVoltage() {
        writeRequest("OUTP OFF")
    }

    fun setMaxCurrent(current: Double) {
        writeRequest("SOUR:CURR $current")
    }


    fun getVolatage(): Int {
        return readRequest("MEASure:VOLTage?")
    }

}
