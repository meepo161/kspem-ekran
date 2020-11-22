package ru.avem.ekran.utils

import javafx.scene.paint.Color
import tornadofx.c

enum class ExperimentType(val type: String) {
    AC("Переменный ток") {
        override fun toString() = type
    },
    DC("Постоянный ток") {
        override fun toString() = type
    }
}

enum class LogTag(val c: Color) {
    MESSAGE(c("#5dbb25")),
    ERROR(c("#ff3935")),
    DEBUG(c("#359eee"))
}

enum class State(val c: Color) {
    OK(c("#00FF0093")),
    INTERMEDIATE(c("#FFFF0093")),
    BAD(c("#FF000093")),
}

class Constants {
    companion object {
        const val OSCILLOSCOPE_DEVICE_NAME = "AKTAKOM ACK-3002"
    }
}

const val KTR = 31

const val YES = "ДА"
const val NO = "НЕТ"

const val SHEET_PASSWORD = "444488888888"

const val KM2_STATE = 0b1
const val A1_PROTECTION_STATE = 0b10
const val A2_PROTECTION_STATE = 0b100
const val A3_PROTECTION_STATE = 0b1000
const val DOOR_STATE = 0b10000

const val BREAK_IKAS = 1.0E9
