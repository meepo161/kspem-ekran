package ru.avem.ekran.communication.model.devices.avem.avem7

import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceModel

class Avem7Model : IDeviceModel {
    companion object {
        const val AMPERAGE = "AMPERAGE"
        const val SHUNT = "SHUNT"
        const val PGA_MODE = "PGA_MODE"
        const val RELAY_STATE = "RELAY_STATE"
    }

    override val registers = mapOf(
        AMPERAGE to DeviceRegister(0x1004, DeviceRegister.RegisterValueType.FLOAT, "мА"),
        SHUNT to DeviceRegister(0x11A0, DeviceRegister.RegisterValueType.FLOAT),
        PGA_MODE to DeviceRegister(0x10C4, DeviceRegister.RegisterValueType.SHORT),
        RELAY_STATE to DeviceRegister(0x1136, DeviceRegister.RegisterValueType.SHORT)
    )

    override fun getRegisterById(idRegister: String) =
        registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")
}
