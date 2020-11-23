package ru.avem.ekran.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.ekran.entities.TableValuesTest5
import ru.avem.ekran.utils.LogTag
import ru.avem.ekran.utils.Singleton
import ru.avem.ekran.view.MainView
import tornadofx.add
import tornadofx.observableList
import tornadofx.style
import java.text.SimpleDateFormat

class Test5Controller : TestController() {

    private lateinit var factoryNumber: String
    val controller: MainViewController by inject()
    val mainView: MainView by inject()

    var tableValues = observableList(
        TableValuesTest5(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),

        TableValuesTest5(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    fun clearTable() {
        tableValues.forEach {
            it.resistanceR.value = "0.0"
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun fillTableByEO() {
        tableValues[0].resistanceR.value = Singleton.currentTestItem.voltageMax
    }

    fun appendMessageToLog(tag: LogTag, _msg: String) {
        val msg = Text("${SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())} | $_msg")
        msg.style {
            fill =
                when (tag) {
                    LogTag.MESSAGE -> tag.c
                    LogTag.ERROR -> tag.c
                    LogTag.DEBUG -> tag.c
                }
        }

        Platform.runLater {
            mainView.vBoxLog.add(msg)
        }
    }

    fun startTest() {
    }
}