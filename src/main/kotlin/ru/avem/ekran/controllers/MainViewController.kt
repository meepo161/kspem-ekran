package ru.avem.ekran.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.ekran.app.Ekran.Companion.forOneInit
import ru.avem.ekran.app.Ekran.Companion.isAppRunning
import ru.avem.ekran.communication.model.CommunicationModel
import ru.avem.ekran.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.ekran.database.entities.ObjectsTypes
import ru.avem.ekran.database.entities.TestObjectsType
import ru.avem.ekran.entities.*
import ru.avem.ekran.utils.*
import ru.avem.ekran.view.MainView
import ru.avem.ekran.view.Styles
import tornadofx.*
import java.text.SimpleDateFormat
import kotlin.concurrent.thread
import kotlin.experimental.and


class MainViewController : TestController() {
    val view: MainView by inject()
    val controller: MainViewController by inject()
    var position1 = ""

    var tableValuesTest1 = observableList(
        TableValuesTest1(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),
        TableValuesTest1(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    var tableValuesTest2 = observableList(
        TableValuesTest2(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),

        TableValuesTest2(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )
    var tableValuesTest3 = observableList(
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

    var tableValuesTest4 = observableList(
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

    var tableValuesTest5 = observableList(
        TableValuesTest5(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),

        TableValuesTest5(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    var cause: String = ""
        set(value) {
            if (value != "") {
                isExperimentRunning = false
                isStopped = true
            }
            field = value
        }

    @Volatile
    var isExperimentRunning: Boolean = false

    @Volatile
    var isExperimentEnded: Boolean = true

    @Volatile
    private var currentVIU: Boolean = false

    @Volatile
    private var startButton: Boolean = false

    @Volatile
    private var stopButton: Boolean = false

    @Volatile
    private var isStopped: Boolean = false

    @Volatile
    private var platform1: Boolean = false

    @Volatile
    private var platform2: Boolean = false

    init {
        if (forOneInit) {
            CommunicationModel.checkDevices()
            CommunicationModel.addWritingRegister(
                CommunicationModel.DeviceID.DD2,
                OwenPrModel.RESET_DOG,
                1.toShort()
            )
            owenPR.initOwenPR()
            owenPR.resetKMS()

            thread(isDaemon = true) {
                while (isAppRunning) {
                    if (owenPR.isResponding) {
                        runLater {
                            view.comIndicate.fill = State.OK.c
                        }
                    } else {
                        runLater {
                            view.comIndicate.fill = State.BAD.c
                        }
                    }
                    sleep(1000)
                }
            }

            forOneInit = false
        }
        startPollDevices()
        runLater {
            view.buttonStop.isDisable = true
        }
    }

    var isDevicesResponding: () -> Boolean = {
        true
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
//            view.buttonStart.isDisable = !platform1 && !platform2
            runLater {
                if (platform1 && platform2) {
                    view.textFieldPlatform.addClass(Styles.redText)
                    view.textFieldPlatform.text = "Ошибка концевиков"
                } else {
                    when {
                        platform1 -> {
                            view.textFieldPlatform.removeClass(Styles.redText)
                            view.textFieldPlatform.text = "Платформа 1"
                        }
                        platform2 -> {
                            view.textFieldPlatform.removeClass(Styles.redText)
                            view.textFieldPlatform.text = "Платформа 2"
                        }
                        else -> {
                            view.textFieldPlatform.addClass(Styles.redText)
                            view.textFieldPlatform.text = "Закройте крышку платформы"
                        }
                    }
                }
            }
        }
    }

    fun handleStartTest() {
        if (view.comboBoxTestItem.selectionModel.isEmpty) {
            runLater {
                Toast.makeText("Выберите объект испытания").show(Toast.ToastType.WARNING)
            }
        } else if (!isAtLeastOneIsSelected()) {
            runLater {
                Toast.makeText("Выберите хотя бы одно испытание из списка").show(Toast.ToastType.WARNING)
            }
        } else {
            Singleton.currentTestItem = transaction {
                TestObjectsType.find {
                    ObjectsTypes.id eq view.comboBoxTestItem.selectedItem!!.id
                }.toList().observable()
            }.first()
            thread(isDaemon = true) {
                runLater {
                    view.buttonStart.isDisable = true
                    view.buttonStop.isDisable = false
                    view.mainMenubar.isDisable = true
                    view.comboBoxTestItem.isDisable = true
                    view.buttonSelectAll.isDisable = true
                }
                clearTable()
                CommunicationModel.clearPollingRegisters()
                isExperimentRunning = true
                if (view.checkBoxTest2.isSelected && isExperimentRunning) {
                    isDevicesResponding = {
                        owenPR.isResponding
                    }
                    runLater {
                        view.checkBoxTest2.isDisable = true
                    }
                    Test2Controller().startTest()
                }
                if (view.checkBoxTest1.isSelected && isExperimentRunning) {
                    isDevicesResponding = {
                        owenPR.isResponding
                    }
                    runLater {
                        view.checkBoxTest1.isDisable = true
                    }
                    Test1Controller().startTest()
                }
                if (view.checkBoxTest4.isSelected && isExperimentRunning) {
                    isDevicesResponding = {
                        owenPR.isResponding
                    }
                    runLater {
                        view.checkBoxTest4.isDisable = true
                    }
                    Test4Controller().startTest()
                }
                if (view.checkBoxTest3.isSelected && isExperimentRunning) {
                    isDevicesResponding = {
                        owenPR.isResponding || deltaCP.isResponding || avem4.isResponding || avem7.isResponding
                    }
                    runLater {
                        view.checkBoxTest3.isDisable = true
                    }
                    Test3Controller().startTest()
                }
                if (view.checkBoxTest5.isSelected && isExperimentRunning) {
                    isDevicesResponding = {
                        owenPR.isResponding
                    }
                    runLater {
                        view.checkBoxTest5.isDisable = true
                    }
                    Test5Controller().startTest()
                }

                appendMessageToLog(LogTag.MESSAGE, "Испытания завершены")
                isExperimentRunning = false
                startPollDevices()
                runLater {
                    view.buttonStart.isDisable = false
                    view.buttonStop.isDisable = true
                    view.mainMenubar.isDisable = false
                    view.comboBoxTestItem.isDisable = false
                    view.buttonSelectAll.isDisable = false
                    view.checkBoxTest1.isDisable = false
                    view.checkBoxTest2.isDisable = false
                    view.checkBoxTest3.isDisable = false
                    view.checkBoxTest4.isDisable = false
                    view.checkBoxTest5.isDisable = false
                }
            }
        }
    }

    fun handleStopTest() {
        cause = "Остановлено оператором"
    }

    private fun appendMessageToLog(tag: LogTag, _msg: String) {
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

    private fun isAtLeastOneIsSelected(): Boolean {
        return view.checkBoxTest1.isSelected ||
                view.checkBoxTest2.isSelected ||
                view.checkBoxTest3.isSelected ||
                view.checkBoxTest4.isSelected ||
                view.checkBoxTest5.isSelected
    }


    fun refreshObjectsTypes() {
        val selectedIndex = view.comboBoxTestItem.selectionModel.selectedIndex
        view.comboBoxTestItem.items = transaction {
            TestObjectsType.all().toList().observable()
        }
        view.comboBoxTestItem.selectionModel.select(selectedIndex)
    }

    fun initTable() {
        runLater {
            tableValuesTest1[0].resistanceAB.value = view.comboBoxTestItem.selectionModel.selectedItem!!.xR
            tableValuesTest1[0].resistanceBC.value = view.comboBoxTestItem.selectionModel.selectedItem!!.xR
            tableValuesTest1[0].resistanceCA.value = view.comboBoxTestItem.selectionModel.selectedItem!!.xR
            tableValuesTest1[0].result.value = ""
            tableValuesTest2[0].resistanceR.value = view.comboBoxTestItem.selectionModel.selectedItem!!.rIsolation
            tableValuesTest2[0].result.value = ""
            tableValuesTest3[0].voltage.value = "500.0"
            tableValuesTest3[0].current.value = "20.0"
            tableValuesTest3[0].result.value = ""
            tableValuesTest4[0].resistanceInductiveAB.value = view.comboBoxTestItem.selectionModel.selectedItem!!.xL
            tableValuesTest4[0].resistanceInductiveBC.value = view.comboBoxTestItem.selectionModel.selectedItem!!.xL
            tableValuesTest4[0].resistanceInductiveCA.value = view.comboBoxTestItem.selectionModel.selectedItem!!.xL
            tableValuesTest4[0].result.value = ""
            tableValuesTest5[0].resistanceR.value = view.comboBoxTestItem.selectionModel.selectedItem!!.xR
            tableValuesTest5[0].resistanceL.value = view.comboBoxTestItem.selectionModel.selectedItem!!.xL
            tableValuesTest5[0].result.value = ""
        }
    }

    fun clearTable() {
        runLater {
            tableValuesTest1[1].resistanceAB.value = ""
            tableValuesTest1[1].resistanceBC.value = ""
            tableValuesTest1[1].resistanceCA.value = ""
            tableValuesTest1[1].result.value = ""
            tableValuesTest2[1].resistanceR.value = ""
            tableValuesTest2[1].result.value = ""
            tableValuesTest3[1].voltage.value = ""
            tableValuesTest3[1].current.value = ""
            tableValuesTest3[1].result.value = ""
            tableValuesTest4[1].resistanceInductiveAB.value = ""
            tableValuesTest4[1].resistanceInductiveBC.value = ""
            tableValuesTest4[1].resistanceInductiveCA.value = ""
            tableValuesTest4[1].result.value = ""
            tableValuesTest5[1].resistanceR.value = ""
            tableValuesTest5[1].resistanceL.value = ""
            tableValuesTest5[1].result.value = ""
        }
    }

    fun showAboutUs() {
        Toast.makeText("Версия ПО: 1.0.0\nВерсия БСУ: 1.0.0\nДата: 30.04.2020").show(Toast.ToastType.INFORMATION)
    }

}
