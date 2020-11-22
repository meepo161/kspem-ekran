package ru.avem.ekran.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ObjectsTypes:  IntIdTable() {
    val serialNumber = varchar("serialNumber", 32)
    val resistanceCoil = varchar("resistanceCoil1", 32)
    val resistanceContactGroup = varchar("resistanceContactGroup", 32)
    val voltageMin = varchar("voltageMin", 32)
    val voltageMax = varchar("voltageMax", 32)
    val timeOff = varchar("timeOff", 32)
}

class TestObjectsType(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<TestObjectsType>(ObjectsTypes)
    var serialNumber by ObjectsTypes.serialNumber
    var resistanceCoil by ObjectsTypes.resistanceCoil
    var resistanceContactGroup by ObjectsTypes.resistanceContactGroup
    var voltageMin by ObjectsTypes.voltageMin
    var voltageMax by ObjectsTypes.voltageMax
    var timeOff by ObjectsTypes.timeOff

    override fun toString(): String {
        return serialNumber
    }
}
