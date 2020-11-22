package ru.avem.ekran.enums

import javafx.scene.paint.Color
import tornadofx.c

enum class Tag(val color: Color) {
    DEBUG(c("cyan")),
    INFO(c(0x00 / 0xFF.toDouble(), 0xFF / 0xFF.toDouble(), 0x00 / 0xFF.toDouble())),
    WARN(c(0xFF / 0xFF.toDouble(), 0xFF / 0xFF.toDouble(), 0x00 / 0xFF.toDouble())),
    ERROR(c(0xFF / 0xFF.toDouble(), 0x00 / 0xFF.toDouble(), 0x00 / 0xFF.toDouble())),
    ASSERT(c(0x98 / 0xFF.toDouble(), 0x76 / 0xFF.toDouble(), 0xAA / 0xFF.toDouble()));
}
