package ru.avem.ekran.communication.adapters.serial

import ru.avem.ekran.communication.Connection
import ru.avem.ekran.communication.adapters.Adapter
import ru.avem.ekran.utils.Log
import ru.avem.ekran.utils.toHexString
import java.io.IOException


class SerialAdapter(override val connection: Connection): Adapter {
    fun write(outputArray: ByteArray): Int {
        var numBytesWrite = 0
        try {
            numBytesWrite = connection.write(outputArray)
            Log.i("SerialAdapter", "Write $numBytesWrite bytes.")
            Log.i("SerialAdapter", "Write " + outputArray.toHexString())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return numBytesWrite
    }

    fun read(inputArray: ByteArray): Int {
        var numBytesRead = 0
        try {
            numBytesRead = connection.read(inputArray)
            Log.i("SerialAdapter", "Read $numBytesRead bytes.")
            Log.i("SerialAdapter", "Read: " + inputArray.toHexString(numBytesRead))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return numBytesRead
    }
}