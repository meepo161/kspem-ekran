package ru.avem.ekran.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.ekran.communication.model.CommunicationModel
import ru.avem.ekran.communication.model.devices.avem.avem4.Avem4Model
import ru.avem.ekran.communication.model.devices.avem.avem7.Avem7Model
import ru.avem.ekran.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.ekran.entities.TableValuesTest3
import ru.avem.ekran.utils.LogTag
import ru.avem.ekran.utils.Singleton.currentTestItem
import ru.avem.ekran.utils.State
import ru.avem.ekran.utils.formatRealNumber
import ru.avem.ekran.utils.sleep
import ru.avem.ekran.view.MainView
import ru.avem.ekran.view.Test3View
import tornadofx.add
import tornadofx.clear
import tornadofx.observableList
import tornadofx.style
import java.text.SimpleDateFormat
import kotlin.concurrent.thread
import kotlin.experimental.and

class Test3Controller : TestController() {

    private lateinit var factoryNumber: String
    val view: Test3View by inject()
    val controller: MainViewController by inject()
    val mainView: MainView by inject()

    private var logBuffer: String? = null
    private var cause: String = ""

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

    @Volatile
    var isExperimentRunning: Boolean = false

    @Volatile
    var isExperimentEnded: Boolean = true

    @Volatile
    private var ikasReadyParam: Float = 0f

    @Volatile
    private var measuringU: Double = 0.0

    @Volatile
    private var measuringI: Double = 0.0

    @Volatile
    private var currentVIU: Boolean = false

    @Volatile
    private var startButton: Boolean = false

    @Volatile
    private var stopButton: Boolean = false

    @Volatile
    private var platform1: Boolean = false

    @Volatile
    private var platform2: Boolean = false

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

    private fun appendOneMessageToLog(tag: LogTag, message: String) {
        if (logBuffer == null || logBuffer != message) {
            logBuffer = message
            appendMessageToLog(tag, message)
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
        this.cause = cause
        if (cause.isNotEmpty()) {
            isExperimentRunning = false
        }
        view.buttonStartStopTest.isDisable = true
    }

    private fun startPollDevices() {
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.FIXED_STATES_REGISTER_1) { value ->
            currentVIU = value.toShort() and 1 > 0
            startButton = value.toShort() and 64 > 0
            stopButton = value.toShort() and 128 > 0
            if (currentVIU) {
                setCause("Сработала токовая защита")
            }
            if (stopButton) {
                setCause("Нажали кнопку СТОП")
            }
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.INSTANT_STATES_REGISTER_2) { value ->
            platform1 = value.toShort() and 4 > 0
            platform2 = value.toShort() and 2 > 0

            if (mainView.comboBoxPlatform.selectionModel.selectedItem == "Платформа 1" && !platform1) {
                setCause("Не закрыта крышка платформы 1")
            }
            if (mainView.comboBoxPlatform.selectionModel.selectedItem == "Платформа 2" && !platform2) {
                setCause("Не закрыта крышка платформы 2")
            }
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.PV21, Avem7Model.AMPERAGE) { value ->
            measuringI = formatRealNumber(value.toDouble() * 5)
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.PV24, Avem4Model.RMS_VOLTAGE) { value ->
            measuringU = formatRealNumber(value.toDouble())
        }
    }

    fun startTest() {
        thread(isDaemon = true) {
            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
            }

            isExperimentRunning = true
            isExperimentEnded = false
            clearLog()
            clearTable()

            if (isExperimentRunning) {
                appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
            }

            while (!isDevicesResponding() && isExperimentRunning) {
                CommunicationModel.checkDevices()
                sleep(100)
            }

            if (isExperimentRunning) {
                CommunicationModel.addWritingRegister(
                    CommunicationModel.DeviceID.DD2,
                    OwenPrModel.RESET_DOG,
                    1.toShort()
                )
                CommunicationModel.addWritingRegister(
                    CommunicationModel.DeviceID.DD2,
                    OwenPrModel.RESET_DOG,
                    0.toShort()
                )
                owenPR.initOwenPR()
                startPollDevices()
                sleep(1000)
            }

//            while (!startButton && isExperimentRunning) {
//                appendOneMessageToLog(LogTag.DEBUG, "Нажмите кнопку ПУСК")
//                sleep(100)
//            }

            if (isExperimentRunning) {
                appendMessageToLog(LogTag.DEBUG, "Подготовка стенда")
                sleep(1000)
                owenPR.onKM1()
                owenPR.onKM30()
                appendMessageToLog(LogTag.DEBUG, "Сбор схемы")
                appendMessageToLog(LogTag.DEBUG, "Подключение контакторов для измерения R")

                if (mainView.comboBoxPlatform.selectionModel.selectedItem == "Платформа 1") {
                    owenPR.onKM31()
                } else if (mainView.comboBoxPlatform.selectionModel.selectedItem == "Платформа 2") {
                    owenPR.onKM32()
                }
            }

            if (isExperimentRunning) {
                sleep(8000)
                deltaCP.setObjectParams(50 * 100, 49 * 10, 50 * 100)
                deltaCP.startObject()
                appendMessageToLog(LogTag.DEBUG, "Дождитесь 15 секунд до завершения...")

                var timer = 15
                while (isExperimentRunning && timer-- > 0 && isDevicesResponding()) {
                    appendMessageToLog(LogTag.DEBUG, "Осталось $timer секунд...")
                    tableValues[1].current.value = measuringI.toString()
                    tableValues[1].voltage.value = measuringU.toString()
                    sleep(1000)
                }
                deltaCP.stopObject()
            }

            setResult()

            controller.tableValuesTest3[1].result.value = tableValues[1].result.value

            finalizeExperiment()
        }
    }

    private fun sleepWhile(timeSecond: Int) {
        var timer = timeSecond * 10
        while (isExperimentRunning && timer-- > 0 && isDevicesResponding()) {
            sleep(100)
        }
    }

    private fun setResult() {
        if (cause.isNotEmpty()) {
            tableValues[1].result.value = "Прервано"
            appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: $cause")
        } else if (!isDevicesResponding()) {
            tableValues[1].result.value = "Прервано"
            appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: потеряна связь с устройствами")
        } else {
            tableValues[1].result.value = "Успешно"
            appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
        }
    }

    private fun finalizeExperiment() {
        isExperimentRunning = false
        isExperimentEnded = true

        owenPR.onKM33()
        sleep(2000)
        owenPR.offAllKMs()
        CommunicationModel.clearPollingRegisters()

        Platform.runLater {
            view.buttonBack.isDisable = false
            view.buttonStartStopTest.text = "Старт"
            view.buttonStartStopTest.isDisable = false
            view.buttonNextTest.isDisable = false
        }
    }
}
