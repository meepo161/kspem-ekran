package ru.avem.ekran.communication.adapters.ack3002.driver

class Constants {
    object ACKScopeDrv {
        val ANY_EDGE = 2
        val CPL_50_OHM = 3
        val CPL_AC = 1
        val CPL_DC = 0
        val CPL_GND = 2
        val D = false
        val LEADING_EDGE = 0
        val MAX_DACCODE = 4095
        val MIN_POSTTRG_LENGTH = 2
        val NEED_RESET_OFFS = 2
        val NEED_RESET_RANGE = 1
        val NEED_RESET_TIME = 8
        val NEED_RESET_TRG = 4
        val RSTATUS_POSTTRIGGER: Byte = 2
        val RSTATUS_READDATA: Byte = 3
        val RSTATUS_ROLL: Byte = 4
        val RSTATUS_UNKNOWN: Byte = 0
        val RSTATUS_WAITTRIGGER: Byte = 1
        val SRC_EXTERNAL = 1
        val SRC_INTERNAL = 0
        val STATUS_CARRY_WRITEADDR: Byte = 32
        val STATUS_COUNTER_DONE: Byte = 1
        val STATUS_DELAY_END: Byte = 4
        val STATUS_POSTTRIGGER: Byte = 8
        val STATUS_REGISTRATION: Byte = 2
        val STATUS_ROLL_MODE: Byte = 16
        val TRAILING_EDGE = 1
        val TRIGGER_AUTO = 0
        val TRIGGER_NORMAL = 1
        val TRIGGER_SCROLL = 3
        val TRIGGER_SINGLE = 2
        val CUBA_COMMAND2 = 2.toByte()
        val CUBA_COMMAND1 = 1.toByte()
    }

    object AULNetConnection {
        val AULNCMDEP_CLOSE = 6
        val AULNCMDEP_GETDEVCOUNT = 8
        val AULNCMDEP_GETUSBNAME = 9
        val AULNCMDEP_ISOPEN = 7
        val AULNCMDEP_OPEN = 5
        val AULNCMDEP_READDATA = 2
        val AULNCMDEP_READSTATUS = 0
        val AULNCMDEP_REQACCESS = 4
        val AULNCMDEP_SENDCOMMAND = 1
        val AULNCMDEP_WRITEDATA = 3
        val BT_ENABLE_REQUEST = 101
    }
}