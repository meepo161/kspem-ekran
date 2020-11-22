package ru.avem.ekran.communication.adapters.modbusrtu.utils

import ru.avem.ekran.communication.adapters.CRC16
import ru.avem.ekran.communication.utils.toShort
import java.nio.ByteBuffer
import java.nio.ByteOrder

object CRC {
    fun sign(b: ByteBuffer) {
        b.order(ByteOrder.LITTLE_ENDIAN)
        b.putShort(calc(b))
        b.order(ByteOrder.BIG_ENDIAN)
    }

    private fun calc(b: ByteBuffer) = CRC16().apply { update(b.array(), 0, b.position()) }.value.toShort()

    fun isValid(dst: ByteArray) = calc(dst, cutEndCount = 2) == (dst[dst.size - 1] to dst[dst.size - 2]).toShort()
    private fun calc(b: ByteArray, cutEndCount: Byte) = CRC16().apply { update(b, 0, b.size - cutEndCount) }.value.toShort()
}
