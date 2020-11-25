package ru.avem.ekran.controllers


import javafx.application.Platform
import javafx.scene.text.Text
import ru.avem.ekran.communication.model.CommunicationModel
import ru.avem.ekran.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.ekran.utils.LogTag
import ru.avem.ekran.utils.formatRealNumber
import ru.avem.ekran.utils.sleep
import ru.avem.ekran.view.MainView
import tornadofx.add
import tornadofx.style
import java.text.SimpleDateFormat
import kotlin.experimental.and

class Test5Controller : TestController() {
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
    private var r: Double = 0.0

    @Volatile
    private var l: Double = 0.0

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

    private fun appendOneMessageToLog(tag: LogTag, message: String) {
        if (logBuffer == null || logBuffer != message) {
            logBuffer = message
            appendMessageToLog(tag, message)
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
    }

    fun startTest() {
        Platform.runLater {
            mainView.buttonStart.text = "Остановить"
            controller.tableValuesTest5[1].resistanceR.value = ""
            controller.tableValuesTest5[1].resistanceL.value = ""
            controller.tableValuesTest5[1].result.value = ""
        }
        controller.isExperimentRunning = true
        isExperimentEnded = false

        val rAB = controller.tableValuesTest1[1].resistanceAB.value.toDouble()
        val rBC = controller.tableValuesTest1[1].resistanceBC.value.toDouble()
        val rCA = controller.tableValuesTest1[1].resistanceCA.value.toDouble()
        r = (rAB + rBC + rCA) / 3
        val lAB = controller.tableValuesTest4[1].resistanceInductiveAB.value.toDouble()
        val lBC = controller.tableValuesTest4[1].resistanceInductiveBC.value.toDouble()
        val lCA = controller.tableValuesTest4[1].resistanceInductiveCA.value.toDouble()
        l = (lAB + lBC + lCA) / 3
        controller.tableValuesTest5[1].resistanceR.value = formatRealNumber(r).toString()
        controller.tableValuesTest5[1].resistanceL.value = formatRealNumber(l).toString()


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
        if (r * 0.98 > l || r * 1.02 > l) {
            controller.tableValuesTest5[1].result.value = "Не годен"
            appendMessageToLog(LogTag.ERROR, "Отличие больше, чем на 2%")
        } else {
            controller.tableValuesTest5[1].result.value = "Годен"
            appendMessageToLog(LogTag.MESSAGE, "Испытания завершены успешно")
        }
    }

    private fun finalizeExperiment() {
        controller.isExperimentRunning = false
        isExperimentEnded = true

        Platform.runLater {
            mainView.buttonStart.text = "Запустить"
            mainView.buttonStart.isDisable = false
        }
    }
}