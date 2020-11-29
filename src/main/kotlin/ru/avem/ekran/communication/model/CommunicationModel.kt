package ru.avem.ekran.communication.model

import ru.avem.ekran.app.Ekran.Companion.isAppRunning
import ru.avem.ekran.communication.Connection
import ru.avem.ekran.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.ekran.communication.adapters.serial.SerialAdapter
import ru.avem.ekran.communication.model.devices.avem.avem4.Avem4Controller
import ru.avem.ekran.communication.model.devices.avem.avem7.Avem7Controller
import ru.avem.ekran.communication.model.devices.bris.m4122.M4122Controller
import ru.avem.ekran.communication.model.devices.delta.DeltaController
import ru.avem.ekran.communication.model.devices.ohmmeter.APPAController
import ru.avem.ekran.communication.model.devices.owen.pr.OwenPrController
import ru.avem.ekran.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.ekran.communication.utils.SerialParameters
import java.lang.Thread.sleep
import kotlin.concurrent.thread

object CommunicationModel {
    @Suppress("UNUSED_PARAMETER")
    enum class DeviceID(description: String) {
        DD2("ОВЕН ПР"),
        PV21("АВЭМ-7-01"),
        PV24("АВЭМ-3-04"),
        UZ91("ДЕЛЬТА"),
        PR65("БРИС"),
        PR61("APPA")
    }

    private var isConnected = false

    private val connection = Connection(
        adapterName = "CP2103 USB to RS-485",
        serialParameters = SerialParameters(8, 0, 1, 38400),
        timeoutRead = 100,
        timeoutWrite = 100
    ).apply {
        connect()
        isConnected = true
    }

    private val connectionAPPA = Connection(
        adapterName = "CP2102 USB to UART Bridge Controller",
        serialParameters = SerialParameters(8, 0, 1, 9600),
        timeoutRead = 25,
        timeoutWrite = 25
    ).apply {
        connect()
        isConnected = true
    }

    private val connectionBRIS = Connection(
        //        adapterName = "USB2.0-Ser!",
        adapterName = "CP2102 USB to UART Bridge Controller-2",
        serialParameters = SerialParameters(8, 0, 1, 9600),
        timeoutRead = 25,
        timeoutWrite = 25
    ).apply {
        connect()
        isConnected = true
    }

    private val modbusAdapter = ModbusRTUAdapter(connection)
    private val appaAdapter = SerialAdapter(connectionAPPA)
    private val brisAdapter = SerialAdapter(connectionBRIS)

    private val deviceControllers: Map<DeviceID, IDeviceController> = mapOf(
        DeviceID.DD2 to OwenPrController(DeviceID.DD2.toString(), modbusAdapter, 2),
        DeviceID.PV21 to Avem7Controller(DeviceID.PV21.toString(), modbusAdapter, 21),
        DeviceID.PV24 to Avem4Controller(DeviceID.PV24.toString(), modbusAdapter, 24),
        DeviceID.UZ91 to DeltaController(DeviceID.UZ91.toString(), modbusAdapter, 91),
        DeviceID.PR61 to APPAController(DeviceID.PR61.toString(), appaAdapter, 61),
        DeviceID.PR65 to M4122Controller(DeviceID.PR65.toString(), brisAdapter, 65)

    )

    init {
        thread(isDaemon = true) {
            while (isAppRunning) {
                if (isConnected) {
                    deviceControllers.values.forEach {
                        it.readPollingRegisters()
                    }
                }
                sleep(1)
            }
        }
        thread(isDaemon = true) {
            while (isAppRunning) {
                if (isConnected) {
                    deviceControllers.values.forEach {
                        it.writeWritingRegisters()
                    }
                }
                sleep(1)
            }
        }
    }

    fun getDeviceById(deviceID: DeviceID) = deviceControllers[deviceID] ?: error("Не определено $deviceID")

    fun startPoll(deviceID: DeviceID, registerID: String, block: (Number) -> Unit) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        register.addObserver { _, arg ->
            block(arg as Number)
        }
        device.addPollingRegister(register)
    }

    fun clearPollingRegisters() {
        deviceControllers.values.forEach(IDeviceController::removeAllPollingRegisters)
        deviceControllers.values.forEach(IDeviceController::removeAllWritingRegisters)
    }

    fun removePollingRegister(deviceID: DeviceID, registerID: String) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        register.deleteObservers()
        device.removePollingRegister(register)
    }

    fun checkDevices(): List<DeviceID> {
        deviceControllers.values.forEach(IDeviceController::checkResponsibility)
        return deviceControllers.filter { !it.value.isResponding }.keys.toList()
    }

    fun addWritingRegister(deviceID: DeviceID, registerID: String, value: Number) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        device.addWritingRegister(register to value)
    }
}
