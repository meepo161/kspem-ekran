package ru.avem.ekran.controllers


import javafx.application.Platform
import javafx.scene.text.Text
import ru.avem.ekran.communication.model.CommunicationModel
import ru.avem.ekran.communication.model.devices.avem.avem4.Avem4Model
import ru.avem.ekran.communication.model.devices.avem.avem7.Avem7Model
import ru.avem.ekran.communication.model.devices.bris.m4122.M4122Controller
import ru.avem.ekran.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.ekran.utils.LogTag
import ru.avem.ekran.utils.formatRealNumber
import ru.avem.ekran.utils.sleep
import ru.avem.ekran.view.MainView
import tornadofx.add
import tornadofx.style
import java.text.SimpleDateFormat
import kotlin.concurrent.thread
import kotlin.experimental.and

class Test4Controller : TestController() {
    private lateinit var factoryNumber: String
    val controller: MainViewController by inject()
    val mainView: MainView by inject()

    private var logBuffer: String? = null
    private var cause: String = ""

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

    private fun startPollDevices() {
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.FIXED_STATES_REGISTER_1) { value ->
            currentVIU = value.toShort() and 1 > 0
            startButton = value.toShort() and 64 > 0
            stopButton = value.toShort() and 128 > 0
            if (currentVIU) {
                controller.setCause("Сработала токовая защита")
            }
            if (stopButton) {
                controller.setCause("Нажали кнопку СТОП")
            }
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.INSTANT_STATES_REGISTER_2) { value ->
            platform1 = value.toShort() and 4 > 0
            platform2 = value.toShort() and 2 > 0

            if (mainView.textFieldPlatform.text == "Платформа 1" && !platform1) {
                controller.setCause("Не закрыта крышка платформы 1")
            }
            if (mainView.textFieldPlatform.text == "Платформа 2" && !platform2) {
                controller.setCause("Не закрыта крышка платформы 2")
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
                mainView.buttonStart.text = "Остановить"
            }

            controller.isExperimentRunning = true
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
                CommunicationModel.addWritingRegister(
                    CommunicationModel.DeviceID.DD2,
                    OwenPrModel.RESET_DOG,
                    0.toShort()
                )
                owenPR.initOwenPR()
                startPollDevices()
                sleep(1000)
            }

//            while (!startButton && controller.isExperimentRunning) {
//                appendOneMessageToLog(LogTag.DEBUG, "Нажмите кнопку ПУСК")
//                sleep(100)
//            }

            val R = bris.setVoltageAndStartMeasuring(type = M4122Controller.MeasuringType.RESISTANCE).toDouble()
            if (R == -1.0) {
                controller.tableValuesTest5[0].resistanceR.value = "Вне диапазона"
            } else {
                controller.tableValuesTest5[0].resistanceR.value = "${R / 1000} МОм"
            }

            setResult()

            finalizeExperiment()
        }
    }

    private fun sleepWhile(timeSecond: Int) {
        var timer = timeSecond * 10
        while (controller.isExperimentRunning && timer-- > 0 && controller.isDevicesResponding()) {
            sleep(100)
        }
    }

    private fun setResult() {
        if (cause.isNotEmpty()) {
            controller.tableValuesTest3[1].result.value = "Прервано"
            appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: $cause")
        } else if (!controller.isDevicesResponding()) {
            controller.tableValuesTest3[1].result.value = "Прервано"
            appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: потеряна связь с устройствами")
        } else {
            controller.tableValuesTest3[1].result.value = "Успешно"
            appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
        }
    }

    private fun finalizeExperiment() {
        controller.isExperimentRunning = false
        isExperimentEnded = true

        owenPR.onKM33()
        sleep(2000)
        owenPR.offAllKMs()
        CommunicationModel.clearPollingRegisters()

        Platform.runLater {
            mainView.buttonStart.text = "Запустить"
            mainView.buttonStart.isDisable = false
        }
    }
}