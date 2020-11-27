package ru.avem.ekran.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import javafx.stage.Modality
import org.slf4j.LoggerFactory
import ru.avem.ekran.controllers.MainViewController
import ru.avem.ekran.database.entities.TestObjectsType
import ru.avem.ekran.entities.*
import ru.avem.ekran.view.Styles.Companion.megaHard
import tornadofx.*
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess


class MainView : View("Комплексный стенд проверки электрических машин") {
    override val configPath: Path = Paths.get("./app.conf")

    private val controller: MainViewController by inject()

    var mainMenubar: MenuBar by singleAssign()
    var comIndicate: Circle by singleAssign()
    var vBoxLog: VBox by singleAssign()

    val checkBoxIntBind = SimpleIntegerProperty() //TODO переименовать нормально

    private var addIcon = ImageView("ru/avem/ekran/icon/add.png")
    private var deleteIcon = ImageView("ru/avem/ekran/icon/delete.png")
    private var editIcon = ImageView("ru/avem/ekran/icon/edit.png")


    var comboBoxTestItem: ComboBox<TestObjectsType> by singleAssign()
    var textFieldPlatform: TextField by singleAssign()


    var buttonStart: Button by singleAssign()
    var buttonStop: Button by singleAssign()
    var checkBoxTest1: CheckBox by singleAssign()
    var checkBoxTest2: CheckBox by singleAssign()
    var checkBoxTest3: CheckBox by singleAssign()
    var checkBoxTest4: CheckBox by singleAssign()
    var checkBoxTest5: CheckBox by singleAssign()
    private val value1 = SimpleBooleanProperty()
    private val value2 = SimpleBooleanProperty()
    private val value3 = SimpleBooleanProperty()

    companion object {
        private val logger = LoggerFactory.getLogger(MainView::class.java)
    }

    override fun onBeforeShow() {
        addIcon.fitHeight = 16.0
        addIcon.fitWidth = 16.0
        deleteIcon.fitHeight = 16.0
        deleteIcon.fitWidth = 16.0
        editIcon.fitHeight = 16.0
        editIcon.fitWidth = 16.0
    }

    override fun onDock() {
        comboBoxTestItem.selectionModel.selectFirst()
        controller.refreshObjectsTypes()
    }

    override val root = borderpane {
        top {
            mainMenubar = menubar {
                menu("Меню") {
                    item("Выход") {
                        action {
                            exitProcess(0)
                        }
                    }
                }
                menu("База данных") {
                    item("Объекты испытания") {
                        action {
                            find<ObjectTypeEditorWindow>().openModal(
                                modality = Modality.WINDOW_MODAL,
                                escapeClosesWindow = true,
                                resizable = false,
                                owner = this@MainView.currentWindow
                            )
                        }
                    }
                }
                menu("Информация") {
                    item("Версия ПО") {
                        action {
                            controller.showAboutUs()
                        }
                    }
                }
            }.addClass(megaHard)
        }
        center {
            anchorpane {
                vbox(spacing = 32.0) {
                    anchorpaneConstraints {
                        leftAnchor = 16.0
                        rightAnchor = 16.0
                        topAnchor = 16.0
                        bottomAnchor = 16.0
                    }
                    alignmentProperty().set(Pos.CENTER)

                    hbox(spacing = 64.0) {
                        alignmentProperty().set(Pos.CENTER)
                        label("Тип двигателя:") {}.addClass(Styles.extraHard)
                        comboBoxTestItem = combobox {
                            prefWidth = 320.0
                            setOnAction {
                                controller.initTable()
                            }
                            addClass(Styles.extraHard)
                        }
                        label("Место:").addClass(Styles.extraHard)
                        textFieldPlatform = textfield {
                            text = ""
                            prefWidth = 320.0
                        }.addClass(Styles.extraHard)
                    }.addClass(Styles.extraHard)
                    hbox(spacing = 64.0) {
                        alignmentProperty().set(Pos.CENTER)
                        button("Выбрать все опыты:") {
                            action {
                                checkBoxTest1.isSelected = true
                                checkBoxTest2.isSelected = true
                                checkBoxTest3.isSelected = true
                                checkBoxTest4.isSelected = true
                                checkBoxTest5.isSelected = true
                            }
                        }
                    }.addClass(Styles.extraHard)
                    hbox(spacing = 16.0) {
                        alignmentProperty().set(Pos.CENTER)
                        vbox(spacing = 16.0) {
                            alignmentProperty().set(Pos.CENTER_LEFT)
                            checkBoxTest1 =
                                checkbox("1. Сопротивление обмоток постоянному току") {
                                    selectedProperty().onChange {
                                        value3.value = it && value2.value
                                    }
                                }.apply { bind(value1) }.addClass(Styles.extraHard)
                            tableview(controller.tableValuesTest1) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                minWidth = 900.0
                                prefWidth = 900.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)
                                column("", TableValuesTest1::descriptor.getter)
                                column("AB, Ом", TableValuesTest1::resistanceAB.getter)
                                column("BC, Ом", TableValuesTest1::resistanceBC.getter)
                                column("CA, Ом", TableValuesTest1::resistanceCA.getter)
                                column("Результат", TableValuesTest1::result.getter)
                            }

                            checkBoxTest2 = checkbox("2. Сопротивление изоляции") {}.addClass(Styles.extraHard)

                            tableview(controller.tableValuesTest2) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                minWidth = 900.0
                                prefWidth = 900.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)
                                column("", TableValuesTest2::descriptor.getter)
                                column("Сопротивление, МОм", TableValuesTest2::resistanceR.getter)
                                column("Результат", TableValuesTest2::result.getter)
                            }

                            checkBoxTest3 =
                                checkbox("3. Электрическая прочность изоляции") { }.addClass(Styles.extraHard)
                            tableview(controller.tableValuesTest3) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                minWidth = 900.0
                                prefWidth = 900.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)
                                column("", TableValuesTest3::descriptor.getter)
                                column("U, В", TableValuesTest3::voltage.getter)
                                column("I, мА", TableValuesTest3::current.getter)
                                column("Результат", TableValuesTest3::result.getter)
                            }
                        }
                        vbox(spacing = 16.0) {
                            alignmentProperty().set(Pos.CENTER_LEFT)
                            checkBoxTest4 =
                                checkbox("4. Межвитковые замыкания, обрывы") {
                                    selectedProperty().onChange {
                                        value3.value = it && value1.value
                                    }
                                }.apply { bind(value2) }.addClass(Styles.extraHard)
                            tableview(controller.tableValuesTest4) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                minWidth = 900.0
                                prefWidth = 900.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)
                                column("", TableValuesTest4::descriptor.getter)
                                column("AB, мH", TableValuesTest4::resistanceInductiveAB.getter)
                                column("BC, мH", TableValuesTest4::resistanceInductiveBC.getter)
                                column("CA, мH", TableValuesTest4::resistanceInductiveCA.getter)
                                column("Результат", TableValuesTest4::result.getter)
                            }

                            checkBoxTest5 =
                                checkbox("5. Правильность соединения обмоток") {
                                    setOnMouseClicked {
                                        if (checkBoxTest5.isSelected) {
                                            value1.value = true
                                            value2.value = true
                                        } else {
                                            value1.value = false
                                            value2.value = false
                                        }
                                    }
                                    selectedProperty().onChange {
                                        if (it) {
                                            value1.value = it
                                            value2.value = it
                                        }
                                    }
                                }.apply { bind(value3) }.addClass(Styles.extraHard)
                            tableview(controller.tableValuesTest5) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                minWidth = 900.0
                                prefWidth = 900.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)
                                column("", TableValuesTest5::descriptor.getter)
                                column("R", TableValuesTest5::resistanceR.getter)
                                column("L", TableValuesTest5::resistanceL.getter)
                                column("Результат", TableValuesTest5::result.getter)
                            }
                            anchorpane {
                                scrollpane {
                                    anchorpaneConstraints {
                                        leftAnchor = 0.0
                                        rightAnchor = 0.0
                                        topAnchor = 0.0
                                        bottomAnchor = 0.0
                                    }
                                    minHeight = 209.0
                                    maxHeight = 209.0
                                    prefHeight = 209.0
                                    minWidth = 900.0
                                    prefWidth = 900.0
                                    vBoxLog = vbox {
                                    }.addClass(megaHard)

                                    vvalueProperty().bind(vBoxLog.heightProperty())
                                }
                            }
                        }
                    }
                    hbox(spacing = 16) {
                        alignment = Pos.CENTER
                        buttonStart = button("Запустить") {
                            prefWidth = 640.0
                            prefHeight = 128.0
                            action {
                                controller.handleStartTest()
                            }
                        }.addClass(Styles.extraHard)
                        buttonStop = button("Остановить") {
                            prefWidth = 640.0
                            prefHeight = 128.0
                            action {
                                controller.handleStopTest()
                            }
                        }.addClass(Styles.extraHard)
                    }
                }
            }
        }
        bottom = hbox(spacing = 32) {
            alignment = Pos.CENTER_LEFT
            comIndicate = circle(radius = 18) {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                    marginLeft = 14.0
                    marginBottom = 8.0
                }
                fill = c("cyan")
                stroke = c("black")
                isSmooth = true
            }
            label(" Связь со стендом") {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                    marginBottom = 8.0
                }
            }
        }
    }.addClass(Styles.blueTheme, megaHard)
}
