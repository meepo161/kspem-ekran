package ru.avem.ekran.communication.adapters.stringascii

import ru.avem.ekran.communication.Connection
import ru.avem.ekran.communication.adapters.Adapter

class StringASCIIAdapter(override val connection: Connection) : Adapter {
    fun read(
        request: String
    ): String {
        val requestNew = request + "\n"
        return connection.read(requestNew).toString()
    }

    fun write(
        request: String
    ) {
        connection.write(StringBuilder(request).append("\n").toString())
    }
}
