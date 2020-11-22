package ru.avem.ekran.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.ekran.communication.model.devices.bris.m4122.M4122Controller
import ru.avem.ekran.entities.TableValuesTest5
import ru.avem.ekran.utils.LogTag
import ru.avem.ekran.utils.Singleton
import ru.avem.ekran.view.Test5View
import tornadofx.add
import tornadofx.clear
import tornadofx.observableList
import tornadofx.style
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test5Controller : TestController() {

    private lateinit var factoryNumber: String
    val view: Test5View by inject()
    val controller: MainViewController by inject()

    var tableValues = observableList(
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

    fun clearTable() {
        tableValues.forEach {
            it.resistance.value = "0.0"
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    fun fillTableByEO() {
        tableValues[0].resistance.value = Singleton.currentTestItem.voltageMax
    }

    fun setExperimentProgress(currentTime: Int, time: Int = 1) {
        Platform.runLater {
            view.progressBarTime.progress = currentTime.toDouble() / time
        }
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
            view.vBoxLog.add(msg)
        }
    }

    fun startTest() {
        thread {

            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
                view.buttonStartStopTest.isDisable = true
            }

            clearLog()
            clearTable()

            val R = bris.setVoltageAndStartMeasuring(type = M4122Controller.MeasuringType.RESISTANCE).toDouble()
            if (R == -1.0) {
                tableValues[1].resistance.value = "Вне диапазона"
            } else {
                tableValues[1].resistance.value = "${R / 1000} МОм"
            }

            controller.tableValuesTest5[0].resistance.value = tableValues[0].resistance.value
            controller.tableValuesTest5[1].resistance.value = tableValues[1].resistance.value
            controller.tableValuesTest5[1].result.value = tableValues[1].result.value

            Platform.runLater {
                view.buttonStartStopTest.isDisable = false
                view.buttonBack.isDisable = false
                view.buttonStartStopTest.text = "Старт"
                view.buttonNextTest.isDisable = false
            }
        }
    }
}