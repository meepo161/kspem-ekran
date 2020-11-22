package ru.avem.ekran.communication.adapters.ack3002.driver

import ru.avem.ekran.communication.adapters.ack3002.driver.ACKScopeDrv.IACKScopeListener
import kotlin.math.min

object Utils {

    fun ByteArray.toHexString(numBytesRead: Int = this.size): String {
        return buildString {
            for ((i, b) in this@toHexString.withIndex()) {
                if (i == numBytesRead) break
                append(Integer.toHexString(b.toInt() and 0xFF).padStart(2, '0') + ' ')
            }
        }.toUpperCase().trim()
    }


    fun limited(min: Double, value: Double, max: Double): Double {
        return if (value < min) {
            min
        } else {
            Math.min(value, max)
        }
    }

    @JvmStatic
    fun limited(min: Int, value: Int, max: Int): Int {
        return if (value < min) {
            min
        } else {
            min(value, max)
        }
    }

    fun uByte(value: Byte): Int {
        return value.toInt() and 255
    }

    interface IAULNetListener {
        fun onANConnect(aulNetConnection: AULNetConnection?)
    }

    enum class TANetInterface {
        aniAUN, aniAUN2
    }

    interface IAULNetListenerIACKScopeListener : IAULNetListener, IACKScopeListener

    fun nextIntFall(var1: FloatArray?, var2: Int, var3: Int, var4: Double): Double {
        var var2 = var2
        while (var2 < var3 - 1) {
            if (var1!![var2].toDouble() > var4 && var1[var2 + 1].toDouble() <= var4) {
                return var2.toDouble() + (var4 - var1[var2].toDouble()) / (var1[var2 + 1] - var1[var2]).toDouble()
            }
            ++var2
        }
        return var3.toDouble()
    }

    fun nextIntRise(var1: FloatArray?, var2: Int, var3: Int, var4: Double): Double {
        var var2 = var2
        while (var2 < var3 - 1) {
            if (var1!![var2] < var4 && var1[var2 + 1].toDouble() >= var4) {
                return var2.toDouble() + (var4 - var1[var2].toDouble()) / (var1[var2 + 1] - var1[var2]).toDouble()
            }
            ++var2
        }
        return var3.toDouble()
    }
}
