package ru.avem.ekran.communication.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import kotlin.experimental.and
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

fun hash(value: String, algorithm: String = "SHA-256"): String {
    val bytes = value.toByteArray()
    val md = MessageDigest.getInstance(algorithm)
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

fun Byte.toHexString() = "0x%x".format(this)
fun Int.toHexString() = "0x%x".format(this)
fun Int.toHexValueString() = "%x".format(this)

fun toHexStr(src: ByteArray): String {
    val builder = StringBuilder()
    for (element in src) {
        val s = Integer.toHexString((element and 0xFF.toByte()).toInt())
        if (s.length < 2) {
            builder.append(0)
        }
        builder.append(s).append(' ')
    }
    return builder.toString().toUpperCase().trim { it <= ' ' }
}

fun String.hexStrToAsciiStr(): String {
    val builder = StringBuilder()
    for (i in this.indices) {
        val s = this.substring(i, i + 1)

        if (s != " ")
            builder.append(s)
    }
    val output = StringBuilder("")
    if (builder.length > 1) {
        var i = 0
        while (i < builder.length) {
            val str = builder.substring(i, i + 2)
            output.append(Integer.parseInt(str.trim { it <= ' ' }, 16).toChar())
            i += 2
        }
    }
    return output.toString()
}


fun trimByteArray(bytes: ByteArray): ByteArray? {
    var i = bytes.size - 1
    while (i >= 0 && bytes[i].toInt() == 0) {
        --i
    }
    return bytes.copyOf(i + 1)
}

fun allocateOrderedByteBuffer(list: List<Short>, order: TypeByteOrder, size: Int): ByteBuffer {
    val firstWord = list.first().toShort()

    return when (size) {
        8 -> TODO()
        4 -> {
            val secondWord = list[1]

            ByteBuffer.allocate(size)
                .putShort(
                    when (order) {
                        TypeByteOrder.BIG_ENDIAN -> firstWord
                        TypeByteOrder.LITTLE_ENDIAN -> firstWord
                        TypeByteOrder.MID_BIG_ENDIAN -> secondWord
                        TypeByteOrder.MID_LITTLE_ENDIAN -> secondWord
                    }
                ).putShort(
                    when (order) {
                        TypeByteOrder.BIG_ENDIAN -> secondWord
                        TypeByteOrder.LITTLE_ENDIAN -> secondWord
                        TypeByteOrder.MID_BIG_ENDIAN -> firstWord
                        TypeByteOrder.MID_LITTLE_ENDIAN -> firstWord
                    }
                )
                .order(
                    when (order) {
                        TypeByteOrder.BIG_ENDIAN -> ByteOrder.BIG_ENDIAN
                        TypeByteOrder.LITTLE_ENDIAN -> ByteOrder.LITTLE_ENDIAN
                        TypeByteOrder.MID_BIG_ENDIAN -> ByteOrder.LITTLE_ENDIAN
                        TypeByteOrder.MID_LITTLE_ENDIAN -> ByteOrder.BIG_ENDIAN
                    }
                ).also { it.flip() }
        }
        2 -> {
            ByteBuffer.allocate(size)
                .putShort(firstWord)
                .order(
                    when (order) {
                        TypeByteOrder.BIG_ENDIAN -> ByteOrder.BIG_ENDIAN
                        TypeByteOrder.LITTLE_ENDIAN -> ByteOrder.LITTLE_ENDIAN
                        TypeByteOrder.MID_BIG_ENDIAN -> ByteOrder.BIG_ENDIAN
                        TypeByteOrder.MID_LITTLE_ENDIAN -> ByteOrder.LITTLE_ENDIAN
                    }
                ).also { it.flip() }
        }
        1 -> {
            ByteBuffer.allocate(size)
                .put(firstWord.toByte())
                .also { it.flip() }
        }
        else -> throw RuntimeException("Указанное значение [$size] не входит в поддерживаемый перечень allocateOrderedByteBuffer()")
    }
}

fun getOrderedBuffer(input: ByteBuffer, order: TypeByteOrder, size: Int = 4): ByteBuffer {
    val output = ByteBuffer.allocate(size)

    when (order) {
        TypeByteOrder.BIG_ENDIAN -> {
            for (i in 0 until size) output.put(input[i])
        }
        TypeByteOrder.LITTLE_ENDIAN -> {
            for (i in size - 1 downTo 0) output.put(input[i])
        }
        TypeByteOrder.MID_BIG_ENDIAN -> {
            output.put(input[1])
            output.put(input[0])
            if (size == 4) {
                output.put(input[3])
                output.put(input[2])
            }
        }
        TypeByteOrder.MID_LITTLE_ENDIAN -> {
            if (size == 4) {
                output.put(input[2])
                output.put(input[3])
            }
            output.put(input[0])
            output.put(input[1])
        }
    }

    output.flip()
    return output
}

fun getOrderedByteBuffer(value: Int, order: TypeByteOrder): Int {
    val input = ByteBuffer.allocate(4)
    input.putInt(value).array()
    return getOrderedBuffer(input, order).int
}

@ExperimentalUnsignedTypes
fun getOrderedByteBuffer(value: UInt, order: TypeByteOrder): Int {
    val input = ByteBuffer.allocate(4)
    input.putInt(value.toInt()).array()
    return getOrderedBuffer(input, order).int
}

fun getOrderedByteBuffer(value: Float, order: TypeByteOrder): Float {
    val input = ByteBuffer.allocate(4)
    input.putFloat(value).array()
    return getOrderedBuffer(input, order).float
}

fun getOrderedByteBuffer(value: Short, order: TypeByteOrder): Short {
    val input = ByteBuffer.allocate(2)
    input.putShort(value).array()
    return getOrderedBuffer(input, order, 2).short
}

@ExperimentalUnsignedTypes
fun getOrderedByteBuffer(value: UShort, order: TypeByteOrder): Short {
    val input = ByteBuffer.allocate(2)
    input.putShort(value.toShort()).array()
    return getOrderedBuffer(input, order, 2).short
}

fun getShortOrdered(inputBuffer: ByteBuffer, order: TypeByteOrder): List<Short> {
    val buff = getOrderedBuffer(inputBuffer, order)

    val output1 = ByteBuffer.allocate(2)
    val output2 = ByteBuffer.allocate(2)
    output1.put(buff[0]).put(buff[1])
    output2.put(buff[2]).put(buff[3])

    output1.flip()
    output2.flip()

    return listOf(output1.short, output2.short)
}


fun getShortOrdered(value: Int, order: TypeByteOrder): List<Short> {
    val input = ByteBuffer.allocate(4)
    input.putInt(value).array()//TODO зачем array() тут и ниже?

    return getShortOrdered(input, order)
}

fun getShortOrdered(value: Float, order: TypeByteOrder): List<Short> {
    val input = ByteBuffer.allocate(4)
    input.putFloat(value).array()

    return getShortOrdered(input, order)
}

fun getShortOrdered(value: Double, order: TypeByteOrder): List<Short> {
    val input = ByteBuffer.allocate(8)
    input.putDouble(value).array()

    return getShortOrdered(input, order)
}

@ExperimentalUnsignedTypes
fun getShortOrdered(value: UInt, order: TypeByteOrder): List<Short> {
    val input = ByteBuffer.allocate(4)
    input.putInt(value.toInt()).array()

    return getShortOrdered(input, order)
}

@ExperimentalTime
fun toHHmmss(time: Long): String {
    return time.milliseconds.toComponents { hours, minutes, seconds, _ ->
        "${padZero(hours)}:${padZero(minutes)}:${padZero(seconds)}"
    }
}

private fun padZero(d: Int) = d.toString().padStart(2, '0')

fun smartSleep(
    mills: Long,
    breakCondition: () -> Boolean = { false },
    pauseCondition: () -> Boolean = { false }
) {
    if (mills > 0) {
        val initNanos = System.nanoTime()
        val stepMills = 10L
        val minusErrorNanos = 3_000_000L
        var iterations = mills / stepMills

        while (iterations >= 0L && !breakCondition()) {
            if (!pauseCondition()) {
                iterations--
            }

            Thread.sleep(stepMills - minusErrorNanos / 1_000_000)
        }

        val plusErrorNanos = (mills * 1_000_000 - (System.nanoTime() - initNanos)) - minusErrorNanos

        if (plusErrorNanos > 0L && !breakCondition()) {
            Thread.sleep(
                (plusErrorNanos / 1_000_000),
                (plusErrorNanos % 1_000_000).toInt()
            )
        }
    }
}

fun ByteArray.toHexString(
    prefix: String = "0x",
    numBytesRead: Int = this.size,
    infix: String = ""
): String {
    return buildString {
        append(prefix)
        for ((i, b) in this@toHexString.withIndex()) {
            if (i == numBytesRead) break
            append(
                Integer.toHexString(b.toInt() and 0xFF)
                    .padStart(2, '0')
                    .toUpperCase() + infix
            )
        }
    }.trim()
}
