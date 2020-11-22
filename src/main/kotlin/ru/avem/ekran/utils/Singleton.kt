package ru.avem.ekran.utils

import ru.avem.ekran.database.entities.Protocol
import ru.avem.ekran.database.entities.TestObjectsType


object Singleton {
    lateinit var currentProtocol: Protocol
    lateinit var currentTestItem: TestObjectsType
}
