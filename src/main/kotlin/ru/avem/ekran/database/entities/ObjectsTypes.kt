package ru.avem.ekran.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ObjectsTypes:  IntIdTable() {
    val testType = varchar("serialNumber", 32)
    val xR = varchar("xR", 32)
    val rIsolation = varchar("rIsolation", 32)
    val xL = varchar("xL", 32)
}

class TestObjectsType(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<TestObjectsType>(ObjectsTypes)
    var testType by ObjectsTypes.testType
    var xR by ObjectsTypes.xR
    var rIsolation by ObjectsTypes.rIsolation
    var xL by ObjectsTypes.xL

    override fun toString(): String {
        return testType
    }
}
