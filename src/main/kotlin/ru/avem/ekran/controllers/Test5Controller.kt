package ru.avem.ekran.controllers


import javafx.application.Platform
import javafx.scene.text.Text
import ru.avem.ekran.utils.LogTag
import ru.avem.ekran.utils.sleep
import ru.avem.ekran.view.MainView
import tornadofx.add
import tornadofx.runLater
import tornadofx.style
import java.text.SimpleDateFormat

class Test5Controller : TestController() {
    private lateinit var factoryNumber: String
    val controller: MainViewController by inject()
    val mainView: MainView by inject()

    private var logBuffer: String? = null

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

    fun startTest() {
        controller.cause = ""
        controller.isExperimentRunning = true
        isExperimentEnded = false
        runLater {
            controller.tableValuesTest5[1].resistanceR.value = ""
            controller.tableValuesTest5[1].resistanceL.value = ""
            controller.tableValuesTest5[1].result.value = ""

            controller.tableValuesTest5[1].resistanceR.value = controller.tableValuesTest1[1].result.value.toString()
            controller.tableValuesTest5[1].resistanceL.value = controller.tableValuesTest4[1].result.value.toString()

            setResult()
        }

        finalizeExperiment()
    }

    private fun sleepWhile(timeSecond: Int) {
        var timer = timeSecond * 10
        while (controller.isExperimentRunning && timer-- > 0 && controller.isDevicesResponding()) {
            sleep(100)
        }
    }

    private fun setResult() {
        if (controller.tableValuesTest1[1].result.value == "Не годен" || controller.tableValuesTest4[1].result.value == "Не годен") {
            controller.tableValuesTest5[1].result.value = "Не годен"
        } else if (controller.tableValuesTest1[1].result.value == "Годен" && controller.tableValuesTest4[1].result.value == "Годен") {
            controller.tableValuesTest5[1].result.value = "Годен"
        } else {
            controller.tableValuesTest5[1].result.value = "Неизвестно"
        }
    }

    private fun finalizeExperiment() {
        controller.isExperimentRunning = false
        isExperimentEnded = true

    }
}