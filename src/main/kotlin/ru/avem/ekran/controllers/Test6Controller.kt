package ru.avem.ekran.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.ekran.communication.model.CommunicationModel
import ru.avem.ekran.communication.model.devices.ohmmeter.APPAModel
import ru.avem.ekran.entities.TableValuesTest6
import ru.avem.ekran.utils.LogTag
import ru.avem.ekran.utils.formatRealNumber
import ru.avem.ekran.utils.sleep
import ru.avem.ekran.view.Test6View
import tornadofx.add
import tornadofx.clear
import tornadofx.observableList
import tornadofx.style
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test6Controller : TestController() {

    private lateinit var factoryNumber: String
    val view: Test6View by inject()
    val controller: MainViewController by inject()
    var resistance: Float = 0.0f

    var tableValues = observableList(
        TableValuesTest6(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("R"),
            SimpleStringProperty("L"),
            SimpleStringProperty("C"),
            SimpleStringProperty("DCR"),
            SimpleStringProperty("")
        ),

        TableValuesTest6(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("R"),
            SimpleStringProperty("L"),
            SimpleStringProperty("C"),
            SimpleStringProperty("DCR"),
            SimpleStringProperty("")
        )
    )

    fun clearTable() {
        tableValues.forEach {
            it.R.value = "0.0"
            it.L.value = "0.0"
            it.C.value = "0.0"
            it.DCR.value = "0.0"
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    fun fillTableByEO() {
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

    private fun startPollDevices() {
        CommunicationModel.startPoll(CommunicationModel.DeviceID.PR61, APPAModel.RESISTANCE_PARAM) { value ->
            resistance = value.toFloat()
        }
    }

    fun startTest() {
        thread {

            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
            }
            clearLog()
            clearTable()
            appendMessageToLog(LogTag.DEBUG, "Инициализация")
            sleep(1000)

            while (true) {
                when {
                    formatRealNumber(appa.getR().toDouble()) == -2.0 -> {
                        tableValues[1].R.value = "Обрыв"
                    }
                    formatRealNumber(appa.getR().toDouble()) == -1.0 -> {
                        tableValues[1].R.value = "Не изм"
                    }
                    else -> {
                        tableValues[1].R.value = formatRealNumber(appa.getR().toDouble()).toString()
                    }
                }
                when {
                    formatRealNumber(appa.getL().toDouble()) == -2.0 -> {
                        tableValues[1].L.value = "Обрыв"
                    }
                    formatRealNumber(appa.getL().toDouble()) == -1.0 -> {
                        tableValues[1].L.value = "Не изм"
                    }
                    else -> {
                        tableValues[1].L.value = formatRealNumber(appa.getL().toDouble()).toString()
                    }
                }
                when {
                    formatRealNumber(appa.getC().toDouble()) == -2.0 -> {
                        tableValues[1].C.value = "Обрыв"
                    }
                    formatRealNumber(appa.getC().toDouble()) == -1.0 -> {
                        tableValues[1].C.value = "Не изм"
                    }
                    else -> {
                        tableValues[1].C.value = formatRealNumber(appa.getC().toDouble()).toString()
                    }
                }
                when {
                    formatRealNumber(appa.getDCR().toDouble()) == -2.0 -> {
                        tableValues[1].DCR.value = "Обрыв"
                    }
                    formatRealNumber(appa.getDCR().toDouble()) == -1.0 -> {
                        tableValues[1].DCR.value = "Не изм"
                    }
                    else -> {
                        tableValues[1].DCR.value = formatRealNumber(appa.getDCR().toDouble()).toString()
                    }
                }
            }

            Platform.runLater {
                view.buttonBack.isDisable = false
                view.buttonStartStopTest.text = "Старт"
                view.buttonNextTest.isDisable = false
            }
        }
    }

}