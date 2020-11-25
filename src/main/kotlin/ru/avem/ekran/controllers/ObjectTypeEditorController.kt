package ru.avem.ekran.controllers

import javafx.collections.ObservableList
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.ekran.database.entities.TestObjectsType
import ru.avem.ekran.view.ObjectTypeEditorWindow
import tornadofx.Controller
import tornadofx.asObservable
import tornadofx.controlsfx.warningNotification
import tornadofx.isDouble

class ObjectTypeEditorController : Controller() {
    private val window: ObjectTypeEditorWindow by inject()

    fun areFieldsValid(): Boolean {
        if (isValuesEmpty()) {
            warningNotification(
                "Заполнение полей",
                "Заполните все поля и повторите снова.",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        if (!isValuesNumber()) {
            warningNotification(
                "Заполнение полей",
                "Неверное значение (не число)",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        if (!isValuesCorrect()) {
            warningNotification(
                "Заполнение полей",
                "Значения не могут быть меньше 0",
                Pos.BOTTOM_CENTER
            )
            return false
        }

        return true
    }

    private fun isValuesCorrect() = window.textfieldXR.text.toString().replace(',', '.').toDouble() > 0 &&
            window.textfieldXL.text.toString().replace(',', '.').toDouble() > 0 &&
            window.textfieldXIsolation.text.toString().replace(',', '.').toDouble() > 0


    private fun isValuesEmpty() = window.textfieldType.text.isNullOrEmpty() ||
            window.textfieldXL.text.isNullOrEmpty() ||
            window.textfieldXIsolation.text.isNullOrEmpty() ||
            window.textfieldXR.text.isNullOrEmpty()


    private fun isValuesNumber() = window.textfieldXL.text.toString().replace(',', '.').isDouble() &&
            window.textfieldXIsolation.text.toString().replace(',', '.').isDouble() &&
            window.textfieldXR.text.toString().replace(',', '.').isDouble()


    fun getObjects(): ObservableList<TestObjectsType> {
        return transaction {
            TestObjectsType.all().toMutableList().asObservable()
        }
    }
}
