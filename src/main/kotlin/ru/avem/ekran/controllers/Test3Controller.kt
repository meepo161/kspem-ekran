package ru.avem.ekran.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.ekran.communication.model.CommunicationModel
import ru.avem.ekran.communication.model.devices.avem.ikas.Ikas8Model
import ru.avem.ekran.entities.TableValuesTest3
import ru.avem.ekran.utils.*
import ru.avem.ekran.utils.Singleton.currentTestItem
import ru.avem.ekran.view.Test3View
import tornadofx.add
import tornadofx.clear
import tornadofx.observableList
import tornadofx.style
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test3Controller : TestController() {

    private lateinit var factoryNumber: String
    val view: Test3View by inject()
    val controller: MainViewController by inject()

    var tableValues = observableList(
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

    @Volatile private var isIkasResponding: Boolean = false

    @Volatile var isExperimentRunning: Boolean = false

    @Volatile var isExperimentEnded: Boolean = true

    @Volatile private var ikasReadyParam: Float = 0f

    @Volatile private var measuringR: Float = 0f

    @Volatile private var statusIkas: Float = 0f

    @Volatile private var testItemR: Double = 0.0

    @Volatile private var measuringR1: Double = 0.0

    @Volatile private var measuringR2: Double = 0.0

    @Volatile private var measuringR3: Double = 0.0

    @Volatile private var measuringR4: Double = 0.0

    @Volatile private var measuringR5: Double = 0.0

    @Volatile private var measuringR6: Double = 0.0

    @Volatile private var measuringR7: Double = 0.0

    @Volatile private var measuringR8: Double = 0.0

    @Volatile private var openContact = true


    fun clearTable() {
        tableValues.forEach {
            it.voltage.value = "0.0"
            it.current.value = "0.0"
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    fun fillTableByEO() {
        tableValues[0].voltage.value = currentTestItem.resistanceContactGroup
        tableValues[0].current.value = currentTestItem.resistanceContactGroup
    }

    fun setExperimentProgress(currentTime: Int, time: Int = 1) {
        Platform.runLater {
            view.progressBarTime.progress = currentTime.toDouble() / time
        }
    }

    fun appendMessageToLog(tag: LogTag, _msg: String) {
        val msg = Text("${SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())} | $_msg")
        msg.style {
            fill = when (tag) {
                LogTag.MESSAGE -> tag.c
                LogTag.ERROR -> tag.c
                LogTag.DEBUG -> tag.c
            }
        }

        Platform.runLater {
            view.vBoxLog.add(msg)
        }
    }

    private fun isDevicesResponding(): Boolean {
        if (owenPR.isResponding) {
            view.circleComStatus.fill = State.OK.c
        } else {
            view.circleComStatus.fill = State.BAD.c
        }
        return owenPR.isResponding
    }

    fun setCause(cause: String) {
        if (cause.isNotEmpty()) {
            isExperimentRunning = false
        }
        view.buttonStartStopTest.isDisable = true
    }

    private fun startPollDevices() {
    }

    fun startTest() {
        testItemR = currentTestItem.resistanceCoil.toDouble()
        thread {
            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
            }

            startPollDevices()
            isExperimentRunning = true
            isExperimentEnded = false

            clearLog()
            clearTable()

            appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
            appendMessageToLog(LogTag.DEBUG, "Подготовка стенда")

            while (!isDevicesResponding() && isExperimentRunning) {
                CommunicationModel.checkDevices()
                sleep(100)
            }

            CommunicationModel.clearPollingRegisters()
            isExperimentRunning = false
            isExperimentEnded = true

            Platform.runLater {
                view.buttonBack.isDisable = false
                view.buttonStartStopTest.text = "Старт"
                view.buttonStartStopTest.isDisable = false
                view.buttonNextTest.isDisable = false
            }
        }
    }



}