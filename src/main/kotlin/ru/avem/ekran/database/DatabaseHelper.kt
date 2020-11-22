package ru.avem.ekran.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.ekran.database.entities.*
import ru.avem.ekran.database.entities.Users.login
import java.sql.Connection

fun validateDB() {
    Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
        SchemaUtils.create(Users, ProtocolsTable, ObjectsTypes)
    }

    transaction {
        if (User.all().count() < 2) {
            val admin = User.find {
                login eq "admin"
            }

            if (admin.empty()) {
                User.new {
                    login = "admin"
                    password = "avem"
                    fullName = "admin"
                }
            }

            if (TestObjectsType.all().count() < 1) {
                TestObjectsType.new {
                    serialNumber = "111111"
                    resistanceCoil = "0.1"
                    resistanceContactGroup = "0.2"
                    voltageMin = "0.3"
                    voltageMax = "0.4"
                    timeOff = "0.5"
                }

                TestObjectsType.new {
                    serialNumber = "222222"
                    resistanceCoil = "1.1"
                    resistanceContactGroup = "1.2"
                    voltageMin = "1.3"
                    voltageMax = "1.4"
                    timeOff = "1.5"
                }

                TestObjectsType.new {
                    serialNumber = "3333333"
                    resistanceCoil = "2.1"
                    resistanceContactGroup = "2.2"
                    voltageMin = "2.3"
                    voltageMax = "2.4"
                    timeOff = "2.5"
                }
            }
        }
    }
}
