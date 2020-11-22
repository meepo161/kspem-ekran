package ru.avem.ekran.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.ekran.communication.adapters.ack3002.driver.ACKScopeDrv
import ru.avem.ekran.entities.TableValuesTest4
import ru.avem.ekran.utils.LogTag
import ru.avem.ekran.utils.Singleton
import ru.avem.ekran.utils.formatRealNumber
import ru.avem.ekran.utils.sleep
import ru.avem.ekran.view.Test4View
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class Test4Controller : Controller(), Observer {

    private lateinit var factoryNumber: String
    val view: Test4View by inject()
    val controller: MainViewController by inject()
    private val pACKScopeDrv: ACKScopeDrv? = null

    var tableValues = observableList(
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

    fun clearTable() {
        tableValues.forEach {
            it.resistanceInductiveAB.value = "0.0"
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    fun fillTableByEO() {
        tableValues[0].resistanceInductiveAB.value = Singleton.currentTestItem.resistanceContactGroup
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
            }

            clearLog()
            clearTable()
            appendMessageToLog(LogTag.DEBUG, "Инициализация")
            sleep(1000)

            appendMessageToLog(LogTag.DEBUG, "Сбор схемы")
            sleep(1000)
            appendMessageToLog(LogTag.DEBUG, "Поднятие напряжения")
            sleep(1000)
            tableValues[1].result.value = "Успешно"

            sleep(1000)
            appendMessageToLog(LogTag.DEBUG, "Испытание завершено")

            controller.tableValuesTest4[0].resistanceInductiveAB.value = tableValues[0].resistanceInductiveAB.value
            controller.tableValuesTest4[1].resistanceInductiveAB.value = tableValues[1].resistanceInductiveAB.value
            controller.tableValuesTest4[1].result.value = tableValues[1].result.value

            Platform.runLater {
                view.buttonBack.isDisable = false
                view.buttonStartStopTest.text = "Старт"
                view.buttonNextTest.isDisable = false
            }
        }
    }

    override fun update(o: Observable, values: Any) {
        val modelId = (values as Array<*>)[0] as Int
        val param = values[1] as Int
        val value = values[2]
    }

    fun initConnection() {
//        var i = 0;
//        while (i < 10) {
//            view.series.data.add(XYChart.Data(i++, nextInt()))
//        }
        pACKScopeDrv?.initConnection()
    }
}