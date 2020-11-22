package ru.avem.ekran.communication.adapters.modbusascii.utils


fun main() {
    val response = byteArrayOf(
        0x01.toByte(),

        0x06.toByte(),

        0x00.toByte(),
        0x01.toByte(),

        0x00.toByte(),
        0x01.toByte(),

        0x00.toByte()
    )

    val response2 = byteArrayOf(
        0x01.toByte(),

        0x06.toByte(),

        0x00.toByte(),
        0x01.toByte(),

        0x00.toByte(),
        0x01.toByte(),

        0x00.toByte()
    )

    println("RESULT for response[${CRC.calc(response)}] but must be [b5]\n")
    println("RESULT for response2[${CRC.calc(response2)}] but must be [b5]\n")
}

