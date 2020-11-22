package ru.avem.ekran.controllers

import javafx.collections.ObservableList
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.ekran.database.entities.TestObjectsType
import ru.avem.ekran.utils.Toast
import ru.avem.ekran.view.ObjectTypeEditorWindow
import tornadofx.Controller
import tornadofx.asObservable
import tornadofx.controlsfx.warningNotification

class ObjectTypeEditorController : Controller() {
    private val window: ObjectTypeEditorWindow by inject()
    private val mainView: MainViewController by inject()

    private fun areFieldsValid(): Boolean {
        if (isValuesEmpty()) {
            warningNotification(
                "Заполнение полей",
                "Заполните все поля и повторите снова.",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        if (!isValuesInt()) {
            warningNotification(
                "Заполнение полей",
                "Можно вводить только целочисленные значения.Проверьте корректность заполнения полей и повторите снова.",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        if (!isValuePressureCorrect()) {
            warningNotification(
                "Заполнение полей",
                "Усилие не может быть меньше 0 кг и больше 600 кг",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        if (!isValueTimeCorrect()) {
            warningNotification(
                "Заполнение полей",
                "Время не может быть меньше 0",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        return true
    }

    private fun isValuePressureCorrect(): Boolean {
        return window.textfieldPressure.text.toDouble() <= 600.0 && window.textfieldPressure.text.toDouble() > 0
    }

    private fun isValueTimeCorrect(): Boolean {
        return window.textfieldTime.text.toDouble() > 0
    }

    private fun isValuesEmpty(): Boolean {
        return window.textfieldPressure.text.isNullOrEmpty() ||
                window.textfieldTime.text.isNullOrEmpty()

    }

    private fun isValuesInt(): Boolean {
        return try {
            window.textfieldPressure.text.toInt()
            window.textfieldTime.text.toInt()
            true
        } catch (e: Exception) {
            Toast.makeText("Неверно заполнены поля").show(Toast.ToastType.ERROR)
            false
        }
    }

    fun getObjects(): ObservableList<TestObjectsType> {
        return transaction {
            TestObjectsType.all().toList().asObservable()
        }
    }

    private fun clearViews() {
        window.textfieldPressure.clear()
        window.textfieldTime.clear()
    }
}
