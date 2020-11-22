package ru.avem.ekran.communication.adapters.utils

class BitVector(size: Int) {
    var size: Int = 0
        private set

    val byteSize: Int
        get() = data.size

    var isMSBAccess = false
        private set

    val isLSBAccess: Boolean
        get() = !isMSBAccess

    private val data: ByteArray

    var bytes: ByteArray
        @Synchronized get() {
            val dest = ByteArray(data.size)
            System.arraycopy(data, 0, dest, 0, dest.size)
            return dest
        }
        set(data) = System.arraycopy(data, 0, this.data, 0, data.size)

    init {
        this.size = if (size % 8 > 0) {
            size / 8 + 1
        } else {
            size / 8
        }

        data = ByteArray(this.size)
    }

    fun toggleAccess() {
        isMSBAccess = !isMSBAccess
    }

    fun getBit(index: Int): Boolean {
        val idx = translateIndex(index)
        return data[byteIndex(idx)].toInt() and (0x01 shl bitIndex(idx)) != 0
    }

    fun setBit(index: Int, b: Boolean) {
        val idx = translateIndex(index)
        val value = if (b) 1 else 0
        val byteNum = byteIndex(idx)
        val bitNum = bitIndex(idx)
        data[byteNum] = ((data[byteNum].toInt() and (0x01 shl bitNum).inv() or (value and 0x01 shl bitNum)).toByte())
    }

    fun forceSize(size: Int) {
        if (size > data.size * 8) {
            throw IllegalArgumentException("Size exceeds byte[] store")
        } else {
            this.size = size
        }
    }

    override fun toString() = buildString {
        for (i in data.indices) {
            var numberOfBitsToPrint = Byte.SIZE_BITS
            val remainingBits = size - i * Byte.SIZE_BITS
            if (remainingBits < Byte.SIZE_BITS) {
                numberOfBitsToPrint = remainingBits
            }

            append(
                "%${numberOfBitsToPrint}s".format(
                    Integer.toBinaryString(data[i].toInt() and 0xFF)
                ).replace(' ', '0')
            )
            append(" ")
        }
    }

    private fun byteIndex(index: Int): Int {
        return if (index < 0 || index >= data.size * 8) {
            throw IndexOutOfBoundsException()
        } else {
            index / 8
        }
    }

    private fun bitIndex(index: Int): Int {
        return if (index < 0 || index >= data.size * 8) {
            throw IndexOutOfBoundsException()
        } else {
            index % 8
        }
    }

    private fun translateIndex(idx: Int): Int {
        return if (isMSBAccess) {
            val mod4 = idx % 4
            val div4 = idx / 4

            if (div4 % 2 != 0) {
                idx + ODD_OFFSETS[mod4]
            } else {
                idx + STRAIGHT_OFFSETS[mod4]
            }
        } else {
            idx
        }
    }

    companion object {
        private val ODD_OFFSETS = intArrayOf(-1, -3, -5, -7)
        private val STRAIGHT_OFFSETS = intArrayOf(7, 5, 3, 1)

        fun createBitVector(data: ByteArray, size: Int): BitVector {
            val bv = BitVector(data.size * 8)
            bv.bytes = data
            bv.size = size
            return bv
        }

        fun createBitVector(data: ByteArray): BitVector {
            val bv = BitVector(data.size * 8)
            bv.bytes = data
            return bv
        }
    }
}
