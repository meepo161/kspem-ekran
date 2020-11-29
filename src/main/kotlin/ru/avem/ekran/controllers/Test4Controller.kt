package ru.avem.ekran.controllers


import javafx.application.Platform
import javafx.scene.text.Text
import ru.avem.ekran.communication.model.CommunicationModel
import ru.avem.ekran.communication.model.devices.ohmmeter.APPAController.Companion.L_MODE
import ru.avem.ekran.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.ekran.utils.*
import ru.avem.ekran.view.MainView
import tornadofx.add
import tornadofx.runLater
import tornadofx.style
import java.text.SimpleDateFormat
import kotlin.experimental.and

class Test4Controller : TestController() {
    val controller: MainViewController by inject()
    val mainView: MainView by inject()

    private var logBuffer: String? = null

    @Volatile
    var isExperimentEnded: Boolean = true

    @Volatile
    private var ikasReadyParam: Float = 0f

    @Volatile
    private var measuringL1: Double = 0.0

    @Volatile
    private var testItemL: Double = 0.0

    @Volatile
    private var measuringL2: Double = 0.0

    @Volatile
    private var measuringL3: Double = 0.0

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
        }
    }

    fun startTest() {
        controller.cause = ""
        testItemL = Singleton.currentTestItem.xL.toDouble()

        isExperimentEnded = false

        if (controller.isExperimentRunning) {
            appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
        }

        while (!controller.isDevicesResponding() && controller.isExperimentRunning) {
            CommunicationModel.checkDevices()
            sleep(100)
        }

        if (controller.isExperimentRunning && controller.isDevicesResponding()) {
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

        if (controller.isExperimentRunning && controller.isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Подготовка стенда")
            appendMessageToLog(LogTag.DEBUG, "Сбор схемы")
            owenPR.onSound()

            appa.getMode()
            sleepWhile(2)
            prepareAPPAForMeasureL()

            if (mainView.textFieldPlatform.text == "Платформа 1") {
                owenPR.onKM11()
            } else if (mainView.textFieldPlatform.text == "Платформа 2") {
                owenPR.onKM12()
            }
        }

        if (controller.isExperimentRunning && controller.isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Подключение контакторов для измерения L AB")
            owenPR.onKM51()
            owenPR.onKM53()
            sleepWhile(6)
        }

        if (controller.isExperimentRunning && controller.isDevicesResponding()) {
            prepareAPPAForMeasureL()
            measuringL1 = formatRealNumber(appa.getL().toDouble())
            if (measuringL1 == -2.0) {
                controller.tableValuesTest4[1].resistanceInductiveAB.value = "Обрыв"
            } else {
                controller.tableValuesTest4[1].resistanceInductiveAB.value = measuringL1.toString()
            }
        }
        if (controller.isExperimentRunning && controller.isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Подключение контакторов для измерения L BC")
            owenPR.offKM51()
            owenPR.offKM53()
            owenPR.onKM52()
            owenPR.onKM54()
            sleepWhile(6)
        }
        if (controller.isExperimentRunning && controller.isDevicesResponding()) {
            prepareAPPAForMeasureL()
            measuringL2 = formatRealNumber(appa.getL().toDouble())
            if (measuringL2 == -2.0) {
                controller.tableValuesTest4[1].resistanceInductiveBC.value = "Обрыв"
            } else {
                controller.tableValuesTest4[1].resistanceInductiveBC.value = measuringL2.toString()
            }
        }
        if (controller.isExperimentRunning && controller.isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Подключение контакторов для измерения L CA")
            owenPR.offKM52()
            owenPR.offKM54()
            owenPR.onKM51()
            owenPR.onKM54()
            sleepWhile(6)
        }
        if (controller.isExperimentRunning && controller.isDevicesResponding()) {
            prepareAPPAForMeasureL()
            measuringL3 = formatRealNumber(appa.getL().toDouble())
            if (measuringL3 == -2.0) {
                controller.tableValuesTest4[1].resistanceInductiveCA.value = "Обрыв"
            } else {
                controller.tableValuesTest4[1].resistanceInductiveCA.value = measuringL3.toString()
            }
        }

        if (controller.isExperimentRunning && controller.isDevicesResponding()) {
            owenPR.onAPPA()
        }

        setResult()

        finalizeExperiment()
    }

    private fun prepareAPPAForMeasureL() {
        var attempts = 10
        while (--attempts > 0 && controller.isExperimentRunning && (!appa.isResponding || appa.getMode() != L_MODE)) {
            while (!appa.isResponding && controller.isExperimentRunning) {
                owenPR.onAPPA()
                sleepWhile(10)
                appa.getMode()
                sleepWhile(4)
            }
            while (appa.getMode() != L_MODE && appa.isResponding && controller.isExperimentRunning) {
                owenPR.changeModeAPPA()
                sleepWhile(4)
            }
            sleepWhile(4)
        }
    }

    private fun sleepWhile(timeSecond: Int) {
        var timer = timeSecond * 10
        while (controller.isExperimentRunning && timer-- > 0) {
            sleep(100)
        }
    }

    private fun setResult() {
        if (!controller.isDevicesResponding()) {
            controller.tableValuesTest4[1].result.value = "Прервано"
            appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: потеряна связь с устройствами")
        } else if (controller.cause.isNotEmpty()) {
            controller.tableValuesTest4[1].result.value = "Прервано"
            appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: ${controller.cause}")
        } else if ((measuringL1 < testItemL * 0.8 || measuringL1 > testItemL * 1.2)
            && (measuringL2 < testItemL * 0.8 || measuringL2 > testItemL * 1.2)
            && (measuringL3 < testItemL * 0.8 || measuringL3 > testItemL * 1.2)
        ) {
            controller.tableValuesTest4[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Индуктивности отличаются более, чем на 20%"
            )
        } else if ((measuringL1 < testItemL * 0.8 || measuringL1 > testItemL * 1.2)
            && (measuringL2 < testItemL * 0.8 || measuringL2 > testItemL * 1.2)
        ) {
            controller.tableValuesTest4[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Индуктивности AB и BC отличаются более, чем на 20%"
            )
        } else if ((measuringL1 < testItemL * 0.8 || measuringL1 > testItemL * 1.2)
            && (measuringL3 < testItemL * 0.8 || measuringL3 > testItemL * 1.2)
        ) {
            controller.tableValuesTest4[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Индуктивности AB и BC отличаются более, чем на 20%"
            )
        } else if ((measuringL1 < testItemL * 0.8 || measuringL1 > testItemL * 1.2)
            && (measuringL2 < testItemL * 0.8 || measuringL2 > testItemL * 1.2)
        ) {
            controller.tableValuesTest4[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Индуктивности отличаются более, чем на 20%"
            )
        } else if (measuringL1 < testItemL * 0.8 || measuringL1 > testItemL * 1.2) {
            controller.tableValuesTest4[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Индуктивность обмотки AB отличается более, чем на 20%"
            )
        } else if (measuringL2 < testItemL * 0.8 || measuringL2 > testItemL * 1.2) {
            controller.tableValuesTest4[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Индуктивность обмотки BC отличается более, чем на 20%"
            )
        } else if (measuringL3 < testItemL * 0.8 || measuringL3 > testItemL * 1.2) {
            controller.tableValuesTest4[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Индуктивность обмотки CA отличается более, чем на 20%"
            )
        } else if (measuringL1 == -2.0) {
            controller.tableValuesTest4[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Обрыв обмотки AB"
            )
        } else if (measuringL2 == -2.0) {
            controller.tableValuesTest4[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Обрыв обмотки BC"
            )
        } else if (measuringL3 == -2.0) {
            controller.tableValuesTest4[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Обрыв обмотки CA"
            )
        } else {
            controller.tableValuesTest4[1].result.value = "Годен"
            appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
        }
    }

    private fun finalizeExperiment() {
        isExperimentEnded = true

        owenPR.onKM33()
        sleep(2000)
        owenPR.offAllKMs()
        CommunicationModel.clearPollingRegisters()
    }
}
