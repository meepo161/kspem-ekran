package ru.avem.ekran.communication.adapters.modbusascii.utils

import ru.avem.ekran.communication.utils.toHexValueString
import ru.avem.ekran.utils.toIntOrDefaultByFormatter
import java.nio.ByteBuffer

object CRC {

    private fun calcString(input: ByteArray): String {
        val lrc = (0 - (input.sum() and 0xFF)) and 0xFF

        val hexLrc = lrc.toHexValueString()

        val sb = StringBuilder()

        if (hexLrc.length == 1) {
            sb.append('0')
        }
        sb.append(hexLrc)

        return sb.toString().toUpperCase()
    }

    fun calc(input: ByteArray): Short {
        val lrc = (0 - (input.sum() and 0xFF)) and 0xFF

        val hexLrc = lrc.toHexValueString()

        val sb = StringBuilder()

        if (hexLrc.length == 1) {
            sb.append('0')
        }
        sb.append(hexLrc)

        val lrcString: String = calcString(input)

        val byteBuffer = ByteBuffer.allocate(2)
        byteBuffer.put(lrcString[0].toByte())
        byteBuffer.put(lrcString[1].toByte())

        byteBuffer.flip()

        return byteBuffer.short
    }

    fun isValid(response: ByteArray): Boolean {
        val dataAscii = response.copyOfRange(1, response.size - 4)

        val dataHex = convertAsciiToHex(dataAscii)
        val expectedLrc = calcString(dataHex)

        val receivedHexString = getShortHexString(response, response.size - 4)

        return expectedLrc == receivedHexString
    }
}

fun getHexString(input: Byte): String {
    val str = input.toInt().toHexValueString()

    val sb = StringBuilder()
    if (str.length == 1) {
        sb.append('0')
    }
    sb.append(str)

    return sb.toString()
}

fun getHexString(input: Short): String {
    val str = input.toInt().toHexValueString()

    val sb = StringBuilder()
    if (str.length == 1) {
        sb.append('0')
    }
    sb.append(str)

    return sb.toString()
}


fun getShortHexString(response: ByteArray, startPosition: Int): String {
    val l0 = response[startPosition]
    val l1 = response[startPosition + 1]

    val sb = StringBuilder()

    sb.append(l0.toChar())
    sb.append(l1.toChar())

    return sb.toString()
}

fun convertToAsciiByteBuffer(input: ByteBuffer): ByteBuffer {
    val result = ByteBuffer.allocate(input.capacity() * 2)

    input.array().forEach {

        val hexLrc = it.toInt().toHexValueString()

        val sb = StringBuilder()

        if (hexLrc.length == 1) {
            sb.append('0')
        }
        sb.append(hexLrc)

        val lrcString = sb.toString().toUpperCase()

        result.put(lrcString[0].toByte())
        result.put(lrcString[1].toByte())
    }

    return result
}


fun convertAsciiToHex(input: ByteBuffer): ByteBuffer {
    val result = ByteBuffer.allocate(input.capacity() / 2)

    input.array().forEachIndexed { index, it ->

        if (index % 2 == 0) {

            val sb = StringBuilder()

            sb.append("0x")
            sb.append(input[index].toChar().toUpperCase())
            sb.append(input[index + 1].toChar().toUpperCase())

            val hexString = sb.toString()

            val intValue: Short = hexString.toIntOrDefaultByFormatter(0).toShort()

            val ch = intValue.toChar()

            val b = ch.toByte()

            result.put(b)
        }
    }

    return result
}

fun convertAsciiToHex(input: ByteArray): ByteArray {
    val result = ByteArray(input.size / 2)

    var i = 0
    input.forEachIndexed { index, it ->

        if (index % 2 == 0) {

            val sb = StringBuilder()

            sb.append("0x")
            sb.append(input[index].toChar().toUpperCase())
            sb.append(input[index + 1].toChar().toUpperCase())

            val hexString = sb.toString()

            val intValue: Short = hexString.toIntOrDefaultByFormatter(0).toShort()

            val ch = intValue.toChar()

            val b = ch.toByte()

            result[i] = b
            i++
        }
    }

    return result
}
