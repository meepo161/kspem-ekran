package ru.avem.ekran.utils

import java.io.IOException
import java.text.SimpleDateFormat

class Log {
    companion object {
        @JvmStatic
        fun d(tag: String?, s: String?) {
            System.out.printf(
                "%s D/%s: %s\n",
                SimpleDateFormat("HH:mm:ss-SSS").format(System.currentTimeMillis()),
                tag,
                s
            )
        }

        fun i(tag: String?, s: String?) {
            System.out.printf(
                "%s I/%s: %s\n",
                SimpleDateFormat("HH:mm:ss-SSS").format(System.currentTimeMillis()),
                tag,
                s
            )
        }

        fun w(tag: String?, s: String?, e: Exception?) {
            System.out.printf(
                "%s W/%s: %s\n",
                SimpleDateFormat("HH:mm:ss-SSS").format(System.currentTimeMillis()),
                tag,
                s
            )
        }

        fun w(tag: String?, e: Exception) {
            System.out.printf(
                "%s W/%s: %s\n",
                SimpleDateFormat("HH:mm:ss-SSS").format(System.currentTimeMillis()),
                tag,
                e.message
            )
        }

        fun e(tag: String?, s: String?, e: IOException) {
            System.out.printf(
                "%s E/%s: %s %s\n",
                SimpleDateFormat("HH:mm:ss-SSS").format(System.currentTimeMillis()),
                s,
                tag,
                e.message
            )
        }
    }
}