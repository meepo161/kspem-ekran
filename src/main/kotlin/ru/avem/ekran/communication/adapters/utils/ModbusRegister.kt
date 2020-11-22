package ru.avem.ekran.communication.adapters.utils

class ModbusRegister {
    private val body: ByteArray = ByteArray(2)

    val value: Int
        get() = ((body[0].toInt() and 0xff) shl 8) or
                (body[1].toInt() and 0xff)

    constructor(value: Short) {
        body[0] = ((value.toInt() shr 8) and 0xff).toByte()
        body[1] = (value.toInt() and 0xff).toByte()
    }

    @ExperimentalUnsignedTypes
    constructor(value: UShort, UShortFlag: Boolean = true) { //костыль для UShort
        body[0] = ((value.toInt() shr 8) and 0xff).toByte()
        body[1] = (value.toInt() and 0xff).toByte()
    }

    constructor(b1: Byte) {
        body[0] = b1
        body[1] = 0
    }

    @ExperimentalUnsignedTypes
    constructor(b1: UByte, UByteFlag: Boolean = true) { //костыль для UByte
        body[0] = b1.toByte()
        body[1] = 0
    }

    constructor(b1: Byte, b2: Byte) {
        body[0] = b1
        body[1] = b2
    }

    constructor(bytes: Pair<Byte, Byte>) {
        body[0] = bytes.first
        body[1] = bytes.second
    }

    constructor(bytes: ByteArray) {
        if (bytes.size == 2) {
            body[0] = bytes[0]
            body[1] = bytes[1]
        } else {
            throw IllegalArgumentException()
        }
    }

    fun toShort() = value.toShort()

    @ExperimentalUnsignedTypes
    fun toUShort() = value.toUShort()

    fun toBytes() = ByteArray(2).also {
        System.arraycopy(body, 0, it, 0, it.size)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModbusRegister

        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        return body.contentHashCode()
    }

    override fun toString() = "$value"
}
