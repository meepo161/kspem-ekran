package ru.avem.ekran.communication.model.devices.instekGPT

import ru.avem.ekran.communication.adapters.stringascii.StringASCIIAdapter
import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceController
import ru.avem.ekran.communication.utils.TransportException
import java.util.*

class InstekController(
    override val id: Byte,
    override val name: String,
    override val protocolAdapter: StringASCIIAdapter
) : IDeviceController {
    val model = InstekModel()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()
    override val pollingMutex = Any()

    override fun readRequest(request: String): String {
        return protocolAdapter.read(request)
    }

    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun writeRequest(request: String) {
        protocolAdapter.write(request)
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
        writeRequest("*idn?")
    }

    fun setVoltageACW(voltage: Double) {
        writeRequest("MANU:ACW:VOLTage $voltage")
    }

    fun setVoltageDCW(voltage: Double) {
        writeRequest("MANU:DCW:VOLTage ${"%.3f".format(Locale.US, voltage)}")
    }

    fun onTest() {
        writeRequest("FUNC:TEST ON")
    }

    fun offTest() {
        writeRequest("FUNC:TEST OFF")
    }

    fun setMaxCurrentACW(current: Double) {
        writeRequest("MANU:ACW:CHIS $current")
    }

    fun setMaxCurrentDCW(current: Double) {
        writeRequest("MANU:DCW:CHIS $current")
    }

    fun setRiseTime(timeRise: Double) {
        writeRequest("MANU:RTIM $timeRise")
    }

    fun setFreq(frequency: Double) {
        writeRequest("MANU:ACW:FREQ $frequency")
    }

    fun setTimeACW(time: Double) {
        writeRequest("MANU:ACW:TTIMe $time")
    }

    fun setTimeDCW(time: Double) {
        writeRequest("MANU:DCW:TTIMe $time")
    }

    fun setNameTest(nameTest: String) {
        writeRequest("MANU:NAME $nameTest")
    }

    fun setMode(mode: Int) {
        if (mode == 0) {
            writeRequest("MANU:EDIT:MODE ACW")
        } else if (mode == 1) {
            writeRequest("MANU:EDIT:MODE DCW")
        }
    }

    fun getMeas(): String {
        return readRequest("MEAS?")
    }

}
