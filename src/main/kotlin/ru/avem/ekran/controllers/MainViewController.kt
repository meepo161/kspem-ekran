package ru.avem.ekran.controllers

import javafx.beans.property.SimpleStringProperty
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.ekran.app.Ekran.Companion.forOneInit
import ru.avem.ekran.app.Ekran.Companion.isAppRunning
import ru.avem.ekran.communication.model.CommunicationModel
import ru.avem.ekran.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.ekran.database.entities.ObjectsTypes
import ru.avem.ekran.database.entities.TestObjectsType
import ru.avem.ekran.entities.*
import ru.avem.ekran.utils.Singleton
import ru.avem.ekran.utils.State
import ru.avem.ekran.utils.Toast
import ru.avem.ekran.utils.sleep
import ru.avem.ekran.view.MainView
import tornadofx.*
import kotlin.concurrent.thread
import kotlin.experimental.and


class MainViewController : TestController() {
    val view: MainView by inject()
    var position1 = ""
    var maskTests = 0

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

    var tableValuesTest6 = observableList(
        TableValuesTest6(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),

        TableValuesTest6(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    private var logBuffer: String? = null
    private var cause: String = ""

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
            CommunicationModel.addWritingRegister(
                CommunicationModel.DeviceID.DD2,
                OwenPrModel.RESET_DOG,
                0.toShort()
            )
            CommunicationModel.addWritingRegister(
                CommunicationModel.DeviceID.DD2,
                OwenPrModel.RESET_TIMER,
                0.toShort()
            )
            CommunicationModel.addWritingRegister(
                CommunicationModel.DeviceID.DD2,
                OwenPrModel.RESET_TIMER,
                1.toShort()
            )
            owenPR.initOwenPR()
            owenPR.resetKMS()
            startPollDevices()

            thread {
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
    }

    fun isDevicesResponding(): Boolean {
        return owenPR.isResponding
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
            view.buttonStart.isDisable = !platform1 && !platform2
            runLater {
                when {
                    platform1 -> {
                        view.textFieldPlatform.text = "Платформа 1"
                    }
                    platform2 -> {
                        view.textFieldPlatform.text = "Платформа 2"
                    }
                    else -> {
                        view.textFieldPlatform.text = "Закройте крышку платформы"
                    }
                }
            }
        }
    }

    fun setCause(cause: String) {
        this.cause = cause
        if (cause.isNotEmpty()) {
            isExperimentRunning = false
        }
        view.buttonStart.isDisable = true
    }

    fun handleStartTest() {
        Singleton.currentTestItem = transaction {
            TestObjectsType.find {
                ObjectsTypes.id eq view.comboBoxTestItem.selectedItem!!.id
            }.toList().observable()
        }.first()

        thread(isDaemon = true) {
            if (view.comboBoxTestItem.selectionModel.isEmpty) {
                runLater {
                    Toast.makeText("Выберите объект испытания").show(Toast.ToastType.WARNING)
                }
            } else if (!isAtLeastOneIsSelected()) {
                runLater {
                    Toast.makeText("Выберите хотя бы одно испытание из списка").show(Toast.ToastType.WARNING)
                }
            }
            if (view.checkBoxTest1.isSelected && isExperimentRunning) {
                Test1Controller().startTest()
            }
            if (view.checkBoxTest2.isSelected) {
                Test2Controller().startTest()
            }
            if (view.checkBoxTest3.isSelected) {
                Test3Controller().startTest()
            }
            if (view.checkBoxTest4.isSelected) {
                Test4Controller().startTest()
            }
            if (view.checkBoxTest5.isSelected) {
                Test5Controller().startTest()
            }
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
            TestObjectsType.all().toList().asObservable()
        }
        view.comboBoxTestItem.selectionModel.select(selectedIndex)
    }

    fun showAboutUs() {
        Toast.makeText("Версия ПО: 1.0.0\nВерсия БСУ: 1.0.0\nДата: 30.04.2020").show(Toast.ToastType.INFORMATION)
    }

    fun onLight() {
        owenPR.onLight()
        owenPR.onSound()
    }

    fun offLight() {
        owenPR.offLight()
        owenPR.offSound()
    }
}
