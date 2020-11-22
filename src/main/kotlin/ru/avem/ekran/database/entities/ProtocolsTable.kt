package ru.avem.ekran.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ProtocolsTable : IntIdTable() {
    val date = varchar("date", 256)
    val dateTime = varchar("dateTime", 256)
    val factoryNumber = varchar("factoryNumber", 256)
    val objectType = varchar("objectType", 256)
    val resistanceCoil1 = varchar("resistanceCoil1", 32)
    val resistanceCoil2 = varchar("resistanceCoil2", 32)
    val resistanceContactGroup1 = varchar("resistanceContactGroup1", 32)
    val resistanceContactGroup2 = varchar("resistanceContactGroup2", 32)
    val resistanceContactGroup3 = varchar("resistanceContactGroup3", 32)
    val resistanceContactGroup4 = varchar("resistanceContactGroup4", 32)
    val resistanceContactGroup5 = varchar("resistanceContactGroup5", 32)
    val resistanceContactGroup6 = varchar("resistanceContactGroup6", 32)
    val resistanceContactGroup7 = varchar("resistanceContactGroup7", 32)
    val resistanceContactGroup8 = varchar("resistanceContactGroup8", 32)
    val voltageMin = varchar("voltageMin", 32)
    val voltageMax = varchar("voltageMax", 32)
    val timeOff = varchar("timeOff", 32)
}

class Protocol(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Protocol>(ProtocolsTable)

    var date by ProtocolsTable.date
    var dateTime    by ProtocolsTable.dateTime
    var factoryNumber by ProtocolsTable.factoryNumber
    var objectType by ProtocolsTable.objectType
    var resistanceCoil1 by ProtocolsTable.resistanceCoil1
    var resistanceCoil2 by ProtocolsTable.resistanceCoil2
    var resistanceContactGroup1 by ProtocolsTable.resistanceContactGroup1
    var resistanceContactGroup2 by ProtocolsTable.resistanceContactGroup2
    var resistanceContactGroup3 by ProtocolsTable.resistanceContactGroup3
    var resistanceContactGroup4 by ProtocolsTable.resistanceContactGroup4
    var resistanceContactGroup5 by ProtocolsTable.resistanceContactGroup5
    var resistanceContactGroup6 by ProtocolsTable.resistanceContactGroup6
    var resistanceContactGroup7 by ProtocolsTable.resistanceContactGroup7
    var resistanceContactGroup8 by ProtocolsTable.resistanceContactGroup8
    var voltageMin by ProtocolsTable.voltageMin
    var voltageMax by ProtocolsTable.voltageMax
    var timeOff by ProtocolsTable.timeOff

    override fun toString(): String {
        return "$id. $factoryNumber:$objectType - $date"
    }
}
