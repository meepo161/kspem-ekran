package ru.avem.ekran.communication.model

import java.util.*

class DeviceRegister(
    val address: Short,
    val valueType: RegisterValueType,
    val unit: String = ""
) : Observable() {
    enum class RegisterValueType {
        SHORT,
        FLOAT,
        INT32
    }

    var value: Number = 0.0
        set(value) {
            field = value
            setChanged()
            notifyObservers(field)
        }
}
