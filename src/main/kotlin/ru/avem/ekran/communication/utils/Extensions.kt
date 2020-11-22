package ru.avem.ekran.communication.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.abs

fun List<Short>.toOrderedFloat64(order: TypeByteOrder) = allocateOrderedByteBuffer(this, order, 4).double

fun List<Short>.toOrderedFloat32(order: TypeByteOrder) = allocateOrderedByteBuffer(this, order, 4).float

fun List<Short>.toOrderedInt64(order: TypeByteOrder) = allocateOrderedByteBuffer(this, order, 4).long

@ExperimentalUnsignedTypes
fun List<Short>.toOrderedUInt64(order: TypeByteOrder) = allocateOrderedByteBuffer(this, order, 4).long.toULong()

fun List<Short>.toOrderedInt32(order: TypeByteOrder) = allocateOrderedByteBuffer(this, order, 4).int

@ExperimentalUnsignedTypes
fun List<Short>.toOrderedUInt32(order: TypeByteOrder) = allocateOrderedByteBuffer(this, order, 4).int.toUInt()

fun List<Short>.toOrderedInt16(order: TypeByteOrder) = allocateOrderedByteBuffer(this, order, 2).short

@ExperimentalUnsignedTypes
fun List<Short>.toOrderedUInt16(order: TypeByteOrder) = allocateOrderedByteBuffer(this, order, 2).short.toUShort()


fun List<Short>.toOrderedInt8(order: TypeByteOrder) = allocateOrderedByteBuffer(this, order, 1).get()

@ExperimentalUnsignedTypes
fun List<Short>.toOrderedUInt8(order: TypeByteOrder) = allocateOrderedByteBuffer(this, order, 1).get().toUByte()

fun Float.toOrdered(order: TypeByteOrder) = getOrderedByteBuffer(this, order)
fun Int.toOrdered(order: TypeByteOrder) = getOrderedByteBuffer(this, order)

@ExperimentalUnsignedTypes
fun UInt.toOrdered(order: TypeByteOrder) = getOrderedByteBuffer(this, order)

fun Short.toOrdered(order: TypeByteOrder) = getOrderedByteBuffer(this, order)

@ExperimentalUnsignedTypes
fun UShort.toOrdered(order: TypeByteOrder) = getOrderedByteBuffer(this, order)

fun Int.getShortOrdered(order: TypeByteOrder) = getShortOrdered(this, order)

@ExperimentalUnsignedTypes
fun UInt.getShortOrdered(order: TypeByteOrder) = getShortOrdered(this, order)
fun Float.getShortOrdered(order: TypeByteOrder) = getShortOrdered(this, order)
fun Double.getShortOrdered(order: TypeByteOrder) = getShortOrdered(this, order)

fun Any?.toStringOrDefault(default: String = "") = this?.let { this.toString() } ?: default
fun Any?.toStringOrNull() = if (this is String) this else null

fun Boolean.check(vararg flags: Boolean): Boolean {
    var result = this
    flags.forEach { result = result && it }
    return result
}

fun Boolean.toLong() = if (this) 1L else 0L
fun Boolean.toInt() = if (this) 1 else 0
fun Boolean.toFloat() = if (this) 1f else 0f
fun Boolean.toDouble() = if (this) 1.0 else 0.0
fun Boolean.toByte() = toInt().toByte()

fun Float.toBoolean() = this != 0.0f
fun Int.toBoolean() = this != 0


fun Float.autoformat(): String = this.toDouble().autoformat()
fun Double.autoformat(): String =
    if (this.toLong().toDouble() == this) {
        "%d".format(Locale.ENGLISH, this.toLong())
    } else {
        with(abs(this)) {
            when {
                this == 0.0 -> "%.0f"
                this < 0.1f -> "%.5f"
                this < 1f -> "%.4f"
                this < 10f -> "%.3f"
                this < 100f -> "%.2f"
                this < 1000f -> "%.1f"
                else -> "%.0f"
            }.format(Locale.ENGLISH, this@autoformat)
        }
    }

fun Int.getRange(offset: Int, length: Int = 1) = (shr(offset) and getMask(length))
private fun getMask(length: Int) = (0xFFFFFFFF).shr(32 - length).toInt()

fun Int.putRange(index: Int, length: Int = 1, value: Int = 1): Int {
    var m = 0xFFFFFFFF shr (32 - length)
    m = m shl index
    m = m.inv()
    var res = this and m.toInt()
    val newM = value shl index
    res = res or newM
    return res
}

fun Pair<Byte, Byte>.toShort(order: ByteOrder = ByteOrder.BIG_ENDIAN) = (ByteBuffer.allocate(2).order(order).put(first).put(second).flip() as ByteBuffer).short
