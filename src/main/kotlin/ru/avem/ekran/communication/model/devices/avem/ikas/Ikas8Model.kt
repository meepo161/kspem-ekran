package ru.avem.ekran.communication.model.devices.avem.ikas

import ru.avem.ekran.communication.model.DeviceRegister
import ru.avem.ekran.communication.model.IDeviceModel

class Ikas8Model : IDeviceModel {
    companion object {
        const val STATUS = "STATUS" //0x00h=Завершено,0x65=Ожидание,0x80=Ошибка,0x81=Ошибка (АЦП),0x82=Ошибка (шунт),0x83=Оошибка (ток),0x84=Ошибка (напряжение),404=Измерение",
        const val PROGRESS = "PROGRESS"
        const val RESIST_MEAS = "RESIST_MEAS"
        const val RESIST_CALC = "RESIST_CALC"
        const val RESIST_TEMP_CORR = "RESIST_TEMP_CORR"
        const val TEMP_OUT = "TEMP_OUT"
        const val TEMP_RADIATOR_1 = "TEMP_RADIATOR_1"
        const val TEMP_RADIATOR_2 = "TEMP_RADIATOR_2"
        const val RESIST_PRE_MEAS = "RESIST_PRE_MEAS"
        const val TIME_MEAS = "TIME_MEAS"
        const val CURRENT_MEAS = "CURRENT_MEAS"
        const val CURRENT_DELTA = "CURRENT_DELTA"
        const val VOLTAGE_MEAS = "VOLTAGE_MEAS"
        const val VOLTAGE_DELTA = "VOLTAGE_DELTA"
        const val START_STOP = "START_STOP" //"0=Остановка,1=Запуск",
        const val CFG_SCHEME = "CFG_SCHEME" //"0x47=AA,0x42=BB,0x43=CC,0x46=AB,0x45=AC,0x44=BC",
        const val CFG_CURRENT_RANGE = "CFG_CURRENT_RANGE"
        const val CFG_RESIST_RANGE = "CFG_RESIST_RANGE"
        const val RESIST_WAIT = "RESIST_WAIT"
        const val TR_TOTAL_POWER = "TR_TOTAL_POWER"
        const val TR_NOMINAL_VOLTAGE = "TR_NOMINAL_VOLTAGE"
        const val SERIAL = "SERIAL"

        const val AA: Int = 0x47
        const val BB: Int = 0x42
        const val CC: Int = 0x43
        const val AB: Int = 0x46
        const val AC: Int = 0x45
        const val BC: Int = 0x44
    }


    override val registers: Map<String, DeviceRegister> = mapOf(
        STATUS to DeviceRegister(0x1000, DeviceRegister.RegisterValueType.INT32),
        PROGRESS to DeviceRegister(0x1001, DeviceRegister.RegisterValueType.INT32),
        RESIST_MEAS to DeviceRegister(0x1002, DeviceRegister.RegisterValueType.FLOAT),
        RESIST_CALC to DeviceRegister(0x1004, DeviceRegister.RegisterValueType.FLOAT),
        RESIST_TEMP_CORR to DeviceRegister(0x1006, DeviceRegister.RegisterValueType.FLOAT),
        TEMP_OUT to DeviceRegister(0x1008, DeviceRegister.RegisterValueType.FLOAT),
        TEMP_RADIATOR_1 to DeviceRegister(0x100A, DeviceRegister.RegisterValueType.FLOAT),
        TEMP_RADIATOR_2 to DeviceRegister(0x100C, DeviceRegister.RegisterValueType.FLOAT),
        RESIST_PRE_MEAS to DeviceRegister(0x100E, DeviceRegister.RegisterValueType.FLOAT),
        TIME_MEAS to DeviceRegister(0x1010, DeviceRegister.RegisterValueType.FLOAT),
        CURRENT_MEAS to DeviceRegister(0x1012, DeviceRegister.RegisterValueType.FLOAT),
        CURRENT_DELTA to DeviceRegister(0x1014, DeviceRegister.RegisterValueType.FLOAT),
        VOLTAGE_MEAS to DeviceRegister(0x1016, DeviceRegister.RegisterValueType.FLOAT),
        VOLTAGE_DELTA to DeviceRegister(0x1018, DeviceRegister.RegisterValueType.FLOAT),
        START_STOP to DeviceRegister(0x10C8, DeviceRegister.RegisterValueType.INT32),
        CFG_SCHEME to DeviceRegister(0x10CA, DeviceRegister.RegisterValueType.INT32),
        CFG_CURRENT_RANGE to DeviceRegister(0x10CC, DeviceRegister.RegisterValueType.INT32),
        CFG_RESIST_RANGE to DeviceRegister(0x10CE, DeviceRegister.RegisterValueType.INT32),
        RESIST_WAIT to DeviceRegister(0x10D0, DeviceRegister.RegisterValueType.INT32),
        TR_TOTAL_POWER to DeviceRegister(0x10D2, DeviceRegister.RegisterValueType.INT32),
        TR_NOMINAL_VOLTAGE to DeviceRegister(0x10D4, DeviceRegister.RegisterValueType.INT32),
        SERIAL to DeviceRegister(0x10D6, DeviceRegister.RegisterValueType.INT32)

        )

    override fun getRegisterById(idRegister: String) =
        registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")
}
