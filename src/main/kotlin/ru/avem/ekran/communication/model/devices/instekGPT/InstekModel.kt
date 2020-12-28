package ru.avem.ekran.communication.model.devices.instekGPT

import ru.avem.ekran.communication.model.IDeviceModel
import ru.avem.ekran.communication.model.DeviceRegister

class InstekModel : IDeviceModel {
    companion object {
    }
    override val registers: Map<String, DeviceRegister> = mapOf(
    )

    override fun getRegisterById(idRegister: String) =
        registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")

}