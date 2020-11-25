package ru.avem.ekran.communication.model.devices.owen.pr

import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceModel

class OwenPrModel : IDeviceModel {
    companion object {
        const val INSTANT_STATES_REGISTER_1 = "INSTANT_STATES_REGISTER_1"
        const val FIXED_STATES_REGISTER_1 = "FIXED_STATES_REGISTER_1"
        const val RES_REGISTER = "RES_REGISTER"
        const val KMS1_REGISTER = "KMS1_REGISTER"
        const val KMS2_REGISTER = "KMS2_REGISTER"
        const val RESET_DOG = "RESET_DOG"
        const val INSTANT_STATES_REGISTER_2 = "INSTANT_STATES_REGISTER_2"
        const val FIXED_STATES_REGISTER_2 = "FIXED_STATES_REGISTER_2"
    }

    override val registers: Map<String, DeviceRegister> = mapOf(
        INSTANT_STATES_REGISTER_1 to DeviceRegister(512, DeviceRegister.RegisterValueType.SHORT),
        FIXED_STATES_REGISTER_1 to DeviceRegister(513, DeviceRegister.RegisterValueType.SHORT),
        RES_REGISTER to DeviceRegister(514, DeviceRegister.RegisterValueType.SHORT),
        KMS1_REGISTER to DeviceRegister(515, DeviceRegister.RegisterValueType.SHORT),
        KMS2_REGISTER to DeviceRegister(516, DeviceRegister.RegisterValueType.SHORT),
        RESET_DOG to DeviceRegister(517, DeviceRegister.RegisterValueType.SHORT),
        INSTANT_STATES_REGISTER_2 to DeviceRegister(520, DeviceRegister.RegisterValueType.SHORT),
        FIXED_STATES_REGISTER_2 to DeviceRegister(521, DeviceRegister.RegisterValueType.SHORT)
    )

    override fun getRegisterById(idRegister: String) =
        registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")

    var outMask: Short = 0
}