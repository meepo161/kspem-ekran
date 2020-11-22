package ru.avem.ekran.controllers

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.ekran.communication.model.CommunicationModel
import ru.avem.ekran.communication.model.devices.owen.pr.OwenPrController
import ru.avem.ekran.database.entities.ObjectsTypes
import ru.avem.ekran.database.entities.TestObjectsType
import ru.avem.ekran.entities.*
import ru.avem.ekran.utils.Singleton
import ru.avem.ekran.utils.Toast
import ru.avem.ekran.view.MainView
import tornadofx.*


class MainViewController : Controller() {
    val view: MainView by inject()
    var position1 = ""
    var maskTests = 0

    var tableValuesTest1 = observableList(
        TableValuesTest1(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),
        TableValuesTest1(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    var tableValuesTest2 = observableList(
        TableValuesTest2(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),

        TableValuesTest2(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )
    var tableValuesTest3 = observableList(
        TableValuesTest3(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),

        TableValuesTest3(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    var tableValuesTest4 = observableList(
        TableValuesTest4(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),

        TableValuesTest4(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    var tableValuesTest5 = observableList(
        TableValuesTest5(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),

        TableValuesTest5(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    var tableValuesTest6 = observableList(
        TableValuesTest6(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),

        TableValuesTest6(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    fun handleStartTest() {

        Singleton.currentTestItem = transaction {
            TestObjectsType.find {
                ObjectsTypes.id eq view.comboBoxTestItem.selectedItem!!.id
            }.toList().observable()
        }.first()

        maskTests = 0
        maskTests = maskTests or if (view.checkBoxTest1.isSelected) 1 else 0
        maskTests = maskTests or if (view.checkBoxTest2.isSelected) 2 else 0
        maskTests = maskTests or if (view.checkBoxTest3.isSelected) 4 else 0
        maskTests = maskTests or if (view.checkBoxTest4.isSelected) 8 else 0
        maskTests = maskTests or if (view.checkBoxTest5.isSelected) 16 else 0
        maskTests = maskTests or if (view.checkBoxTest6.isSelected) 32 else 0

        if (view.textFieldSerialNumber.text.isEmpty() || view.comboBoxTestItem.selectionModel.isEmpty) {
            Toast.makeText("Введите заводской номер и выберите объект испытания").show(Toast.ToastType.WARNING)
        } else if (!isAtLeastOneIsSelected()) {
            Toast.makeText("Выберите хотя бы одно испытание из списка").show(Toast.ToastType.WARNING)
        } else {
//                experimentsValuesModel.createNewProtocol(
//                    textFieldSerialNumber.getText(),
//                    comboBoxTestItem.getSelectionModel().getSelectedItem()
//                )
            when {
                maskTests and 1 > 0 -> {
                    view.start1Test()
                }
                maskTests and 2 > 0 -> {
                    view.start2Test()
                }
                maskTests and 4 > 0 -> {
                    view.start3Test()
                }
                maskTests and 8 > 0 -> {
                    view.start4Test()
                }
                maskTests and 16 > 0 -> {
                    view.start5Test()
                }
                maskTests and 32 > 0 -> {
                    view.start6Test()
                }
            }
        }
    }

    private fun isAtLeastOneIsSelected(): Boolean {
        return view.checkBoxTest1.isSelected ||
                view.checkBoxTest2.isSelected ||
                view.checkBoxTest3.isSelected ||
                view.checkBoxTest4.isSelected ||
                view.checkBoxTest5.isSelected ||
                view.checkBoxTest6.isSelected
    }

    fun refreshObjectsTypes() {
        val selectedIndex = view.comboBoxTestItem.selectionModel.selectedIndex
        view.comboBoxTestItem.items = transaction {
            TestObjectsType.all().toList().asObservable()
        }
        view.comboBoxTestItem.selectionModel.select(selectedIndex)
    }

    fun showAboutUs() {
        Toast.makeText("Версия ПО: 1.0.0\nВерсия БСУ: 1.0.0\nДата: 30.04.2020").show(Toast.ToastType.INFORMATION)
    }
}
