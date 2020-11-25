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
                    testType = "111111"
                    xR = "0.1"
                    xL = "0.3"
                    rIsolation = "0.34"
                }

                TestObjectsType.new {
                    testType = "222222"
                    xR = "1.1"
                    xL = "1.3"
                    rIsolation = "0.45"
                }

                TestObjectsType.new {
                    testType = "3333333"
                    xR = "2.1"
                    xL = "2.3"
                    rIsolation = "0.51"
                }
            }
        }
    }
}
