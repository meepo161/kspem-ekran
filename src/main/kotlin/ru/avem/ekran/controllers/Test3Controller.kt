package ru.avem.ekran.controllers

import javafx.application.Platform
import javafx.scene.text.Text
import org.slf4j.LoggerFactory
import ru.avem.ekran.communication.model.CommunicationModel
import ru.avem.ekran.communication.model.devices.avem.avem4.Avem4Model
import ru.avem.ekran.communication.model.devices.avem.avem7.Avem7Model
import ru.avem.ekran.communication.model.devices.delta.DeltaModel
import ru.avem.ekran.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.ekran.utils.LogTag
import ru.avem.ekran.utils.Toast
import ru.avem.ekran.utils.formatRealNumber
import ru.avem.ekran.utils.sleep
import ru.avem.ekran.view.MainView
import tornadofx.add
import tornadofx.runLater
import tornadofx.style
import java.text.SimpleDateFormat
import kotlin.experimental.and

class Test3Controller : TestController() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var factoryNumber: String
    val controller: MainViewController by inject()
    val mainView: MainView by inject()

    private var logBuffer: String? = null

    @Volatile
    var isExperimentEnded: Boolean = true

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

    @Volatile
    private var isDeltaNeed: Boolean = false

    @Volatile
    private var isDeltaResponding: Double = 0.0

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
            mainView.vBoxLog.add(msg)
        }
    }

    private fun appendOneMessageToLog(tag: LogTag, message: String) {
        if (logBuffer == null || logBuffer != message) {
            logBuffer = message
            appendMessageToLog(tag, message)
        }
    }

    private fun startPollDevices() {
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.FIXED_STATES_REGISTER_1) { value ->
            currentVIU = value.toShort() and 16 > 0
            startButton = value.toShort() and 64 > 0
            stopButton = value.toShort() and 128 > 0
            if (currentVIU) {
                controller.cause = "Сработала токовая защита"
            }
            if (stopButton) {
                controller.cause = "Нажали кнопку СТОП"
            }
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.INSTANT_STATES_REGISTER_2) { value ->
            platform1 = value.toShort() and 4 > 0
            platform2 = value.toShort() and 2 > 0

            if (mainView.textFieldPlatform.text == "Платформа 1" && !platform1) {
                controller.cause = "Не закрыта крышка платформы 1"
            }
            if (mainView.textFieldPlatform.text == "Платформа 2" && !platform2) {
                controller.cause = "Не закрыта крышка платформы 2"
            }
            if (platform1 && platform2) {
                controller.cause = "Ошибка в работе концевиков"
            }
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.PV21, Avem7Model.AMPERAGE) { value ->
            measuringI = formatRealNumber(value.toDouble() * (1.toDouble() / 5.toDouble()))
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.PV24, Avem4Model.RMS_VOLTAGE) { value ->
            measuringU = formatRealNumber(value.toDouble())
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.UZ91, DeltaModel.STATUS_REGISTER) { value ->
            isDeltaResponding = value.toDouble()
        }
    }

    fun startTest() {
        controller.cause = ""
        Platform.runLater {
            controller.tableValuesTest3[1].voltage.value = ""
            controller.tableValuesTest3[1].current.value = ""
            controller.tableValuesTest3[1].result.value = ""
        }

        isExperimentEnded = false

        if (controller.isExperimentRunning) {
            appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
        }

        while (!controller.isDevicesResponding() && controller.isExperimentRunning) {
            CommunicationModel.checkDevices()
            sleep(100)
        }

        if (controller.isExperimentRunning) {
            CommunicationModel.addWritingRegister(
                CommunicationModel.DeviceID.DD2,
                OwenPrModel.RESET_DOG,
                1.toShort()
            )
            owenPR.initOwenPR()
            startPollDevices()
            sleep(1000)
        }
        if (!startButton && controller.isExperimentRunning && controller.isDevicesResponding()) {
            runLater {
                Toast.makeText("Нажмите кнопку ПУСК").show(Toast.ToastType.WARNING)
            }
        }
        var timeToStart = 300
        while (!startButton && controller.isExperimentRunning && controller.isDevicesResponding() && timeToStart-- > 0) {
            appendOneMessageToLog(LogTag.DEBUG, "Нажмите кнопку ПУСК")
            sleep(100)
        }

        if (!startButton) {
            controller.cause = "Не нажата кнопка ПУСК"
        }

        if (controller.isExperimentRunning) {
            appendMessageToLog(LogTag.DEBUG, "Подготовка стенда")
            sleep(1000)
            owenPR.onKM1()
            owenPR.onKM30()
            appendMessageToLog(LogTag.DEBUG, "Сбор схемы")
            owenPR.onSound()

            if (mainView.textFieldPlatform.text == "Платформа 1") {
                owenPR.onKM31()
            } else if (mainView.textFieldPlatform.text == "Платформа 2") {
                owenPR.onKM32()
            }
        }

        if (controller.isExperimentRunning) {
            appendMessageToLog(LogTag.DEBUG, "Загрузка ЧП...")
            sleepWhile(8)
            deltaCP.setObjectParams(50 * 100, 49 * 10, 50 * 100)
            deltaCP.startObject()
            sleep(1000)
            appendMessageToLog(LogTag.DEBUG, "Дождитесь 15 секунд до завершения...")
            var timer = 15
            while (controller.isExperimentRunning && timer-- > 0 && controller.isDevicesResponding()) {
                appendMessageToLog(LogTag.DEBUG, "Осталось $timer секунд...")
                if (timer < 12 && (measuringU * 0.5 > 500 || measuringU * 1.5 < 500)) {
                    controller.cause = "Необходимое напряжение не удалось выставить"
                }
                if (timer < 12 && measuringI > 20) {
                    controller.cause = "Ток превысил 20 мА"
                }
                runLater {
                    controller.tableValuesTest3[1].current.value = measuringI.toString()
                    controller.tableValuesTest3[1].voltage.value = measuringU.toString()
                }
                sleep(1000)
            }
            deltaCP.stopObject()
        }

        setResult()
        finalizeExperiment()
    }

    private fun sleepWhile(timeSecond: Int) {
        var timer = timeSecond * 10
        while (controller.isExperimentRunning && timer-- > 0 && controller.isDevicesResponding()) {
            sleep(100)
        }
    }

    private fun setResult() {
        runLater {
            if (!controller.isDevicesResponding()) {
                controller.tableValuesTest3[1].result.value = "Прервано"
                appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: потеряна связь с устройствами")
            } else if (!deltaCP.isResponding) {
                controller.tableValuesTest3[1].result.value = "Прервано"
                appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: нет связи с ЧП")
            } else if (controller.cause.isNotEmpty()) {
                controller.tableValuesTest3[1].result.value = "Не годен"
                appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: ${controller.cause}")
            } else if (currentVIU) {
                controller.tableValuesTest3[1].result.value = "Не годен"
                appendMessageToLog(LogTag.ERROR, "Испытание неупешно по причине: пробой изоляции")
            } else {
                controller.tableValuesTest3[1].result.value = "Годен"
                appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
            }
        }
    }

    private fun finalizeExperiment() {
        isExperimentEnded = true
        isDeltaNeed = false

        sleep(2000)
        owenPR.offKM30()
        sleep(1000)
        owenPR.onKM33()
        sleep(2000)
        owenPR.offAllKMs()
        CommunicationModel.clearPollingRegisters()

    }
}
