package ru.avem.ekran.communication.utils

enum class TypeByteOrder(val value: String) {
    BIG_ENDIAN("ABCD"),
    LITTLE_ENDIAN("DCBA"),
    MID_BIG_ENDIAN("BADC"),
    MID_LITTLE_ENDIAN("CDAB");
}
