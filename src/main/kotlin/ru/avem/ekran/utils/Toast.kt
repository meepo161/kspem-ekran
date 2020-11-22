package ru.avem.ekran.utils

import javafx.geometry.Pos
import org.controlsfx.control.Notifications

class Toast private constructor(private val notifications: Notifications) {
    fun show(type: ToastType?) {
        when (type) {
            ToastType.INFORMATION -> {
                notifications.title("Информация")
                notifications.darkStyle()
                notifications.showInformation()
            }
            ToastType.CONFIRM -> {
                notifications.title("Подтверждение")
                notifications.darkStyle()
                notifications.showConfirm()
            }
            ToastType.ERROR -> {
                notifications.title("Ошибка")
                notifications.darkStyle()
                notifications.showError()
            }
            ToastType.WARNING -> {
                notifications.title("Внимание")
                notifications.darkStyle()
                notifications.showWarning()
            }
            ToastType.NONE -> notifications.show()
            else -> notifications.show()
        }
    }

    enum class ToastType {
        INFORMATION, CONFIRM, ERROR, WARNING, NONE
    }

    companion object {
        fun makeText(text: String?): Toast {
            return Toast(Notifications.create().text(text).position(Pos.BOTTOM_CENTER))
        }
    }
}
