package ru.avem.ekran.communication.model.devices.rele

import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceModel

class ReleModel : IDeviceModel {
    companion object {
        const val R0 =  "R0"
        const val R1 =  "R1"
        const val R2 =  "R2"
        const val R3 =  "R3"
        const val R4 =  "R4"
        const val R5 =  "R5"
        const val R6 =  "R6"
        const val R7 =  "R7"
        const val R8 =  "R8"
        const val R9 =  "R9"
        const val R10 = "R10"
        const val R11 = "R11"
        const val R12 = "R12"
        const val R13 = "R13"
        const val R14 = "R14"
        const val R15 = "R15"
        const val R16 = "R16"
        const val R17 = "R17"
        const val R18 = "R18"
        const val R19 = "R19"
        const val R20 = "R20"
        const val R21 = "R21"
        const val R22 = "R22"
        const val R23 = "R23"
        const val R24 = "R24"
        const val R25 = "R25"
        const val R26 = "R26"
        const val R27 = "R27"
        const val R28 = "R28"
        const val R29 = "R29"
        const val R30 = "R30"
        const val R31 = "R31"
        const val R32 = "R32"
    }

    override val registers: Map<String, DeviceRegister> = mapOf(
        R0 to DeviceRegister(0, DeviceRegister.RegisterValueType.SHORT),
        R1 to DeviceRegister(1, DeviceRegister.RegisterValueType.SHORT),
        R2 to DeviceRegister(2, DeviceRegister.RegisterValueType.SHORT),
        R3 to DeviceRegister(3, DeviceRegister.RegisterValueType.SHORT),
        R4 to DeviceRegister(4, DeviceRegister.RegisterValueType.SHORT),
        R5 to DeviceRegister(5, DeviceRegister.RegisterValueType.SHORT),
        R6 to DeviceRegister(6, DeviceRegister.RegisterValueType.SHORT),
        R7 to DeviceRegister(7, DeviceRegister.RegisterValueType.SHORT),
        R8 to DeviceRegister(8, DeviceRegister.RegisterValueType.SHORT),
        R9 to DeviceRegister(9, DeviceRegister.RegisterValueType.SHORT),
        R10 to DeviceRegister(10, DeviceRegister.RegisterValueType.SHORT),
        R11 to DeviceRegister(11, DeviceRegister.RegisterValueType.SHORT),
        R12 to DeviceRegister(12, DeviceRegister.RegisterValueType.SHORT),
        R13 to DeviceRegister(13, DeviceRegister.RegisterValueType.SHORT),
        R14 to DeviceRegister(14, DeviceRegister.RegisterValueType.SHORT),
        R15 to DeviceRegister(15, DeviceRegister.RegisterValueType.SHORT),
        R16 to DeviceRegister(16, DeviceRegister.RegisterValueType.SHORT),
        R17 to DeviceRegister(17, DeviceRegister.RegisterValueType.SHORT),
        R18 to DeviceRegister(18, DeviceRegister.RegisterValueType.SHORT),
        R19 to DeviceRegister(19, DeviceRegister.RegisterValueType.SHORT),
        R20 to DeviceRegister(20, DeviceRegister.RegisterValueType.SHORT),
        R21 to DeviceRegister(21, DeviceRegister.RegisterValueType.SHORT),
        R22 to DeviceRegister(22, DeviceRegister.RegisterValueType.SHORT),
        R23 to DeviceRegister(23, DeviceRegister.RegisterValueType.SHORT),
        R24 to DeviceRegister(24, DeviceRegister.RegisterValueType.SHORT),
        R25 to DeviceRegister(25, DeviceRegister.RegisterValueType.SHORT),
        R26 to DeviceRegister(26, DeviceRegister.RegisterValueType.SHORT),
        R27 to DeviceRegister(27, DeviceRegister.RegisterValueType.SHORT),
        R28 to DeviceRegister(28, DeviceRegister.RegisterValueType.SHORT),
        R29 to DeviceRegister(29, DeviceRegister.RegisterValueType.SHORT),
        R30 to DeviceRegister(30, DeviceRegister.RegisterValueType.SHORT),
        R31 to DeviceRegister(31, DeviceRegister.RegisterValueType.SHORT),
        R32 to DeviceRegister(32, DeviceRegister.RegisterValueType.SHORT)
    )

    override fun getRegisterById(idRegister: String) =
        registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")
}