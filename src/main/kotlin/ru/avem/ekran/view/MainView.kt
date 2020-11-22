package ru.avem.ekran.view

import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.shape.Circle
import javafx.stage.Modality
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import ru.avem.ekran.controllers.*
import ru.avem.ekran.database.entities.TestObjectsType
import ru.avem.ekran.entities.*
import ru.avem.ekran.utils.transitionLeft
import ru.avem.ekran.view.Styles.Companion.megaHard
import tornadofx.*
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class MainView : View("Комплексный стенд проверки электрических машин") {
    override val configPath: Path = Paths.get("./app.conf")

    //    var ackViewFXML: FXMLLoader = FXMLLoader(URL("file:///C:/Users/meepo/IdeaProjects/rele2/src/main/resources/ru/avem/ekran/layout/ackView.fxml"))
//    var ackViewFXML: InputStream = this::class.java.classLoader.getResourceAsStream("./layout/ackView.fxml")

    private val controller: MainViewController by inject()

    var mainMenubar: MenuBar by singleAssign()
    var comIndicate: Circle by singleAssign()

    private var imgPressure: ImageView by singleAssign()
    private var img: ImageView by singleAssign()

    private var addIcon = ImageView("ru/avem/ekran/icon/add.png")
    private var deleteIcon = ImageView("ru/avem/ekran/icon/delete.png")
    private var editIcon = ImageView("ru/avem/ekran/icon/edit.png")
    private var pressureJPG = Image("ru/avem/ekran/icon/pressure.jpg", 400.0, 280.0, false, true)


    var comboBoxTestItem: ComboBox<TestObjectsType> by singleAssign()
    var comboBoxPlatform: ComboBox<String> by singleAssign()


    var buttonStart: Button by singleAssign()
    var buttonSelectAll: Button by singleAssign()
    var checkBoxTest1: CheckBox by singleAssign()
    var checkBoxTest2: CheckBox by singleAssign()
    var checkBoxTest3: CheckBox by singleAssign()
    var checkBoxTest4: CheckBox by singleAssign()
    var checkBoxTest5: CheckBox by singleAssign()
    var checkBoxTest6: CheckBox by singleAssign()
    var textFieldSerialNumber: TextField by singleAssign()

    var test1Modal: Stage = Stage()

    var test1Controller = Test1Controller()
    var test2Controller = Test2Controller()
    var test3Controller = Test4Controller()
    var test4Controller = Test5Controller()
    var test5Controller = Test6Controller()


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
        controller.refreshObjectsTypes()
        comboBoxTestItem.selectionModel.selectFirst()
    }

    override val root = borderpane {
        top {
            mainMenubar = menubar {
                menu("Меню") {
                    item("Очистить") {
                        action {}
                    }
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
                    item("Протоколы") {
                        action {
                            find<ProtocolListWindow>().openModal(
                                modality = Modality.APPLICATION_MODAL,
                                escapeClosesWindow = true,
                                resizable = false,
                                owner = this@MainView.currentWindow
                            )
                        }
                    }
                }
                menu("Отладка") {
                    item("Связь с приборами") {
                        action {
                            find<DevicesView>().openModal(
                                modality = Modality.APPLICATION_MODAL,
                                escapeClosesWindow = true,
                                resizable = false,
                                owner = this@MainView.currentWindow
                            )
                        }
                    }
                    item("Осцилограф") {
                        action {
                            find<AAOPView>().openModal(
                                modality = Modality.NONE,
                                escapeClosesWindow = true,
                                resizable = true,
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
            tabpane {
                tab("Испытания") {
                    isClosable = false
                    anchorpane {
//                img = imageview(Image("back2.png"))
//                img.fitHeight = Screen.getPrimary().bounds.height - 400
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
                                vbox(spacing = 16.0) {
                                    alignmentProperty().set(Pos.CENTER)
                                    label("Серийный номер:")
                                    textFieldSerialNumber = textfield {
                                        prefWidth = 640.0
                                        text = "1234"
                                    }
                                    label("Выберите тип двигателя:")
                                    comboBoxTestItem = combobox {
                                        prefWidth = 640.0
                                    }
                                    label("Выберите платформу:")
                                    comboBoxPlatform = combobox {
                                        prefWidth = 640.0
                                        items = observableListOf("Платформа 1", "Платформа 2")
                                    }
                                }
                                vbox(spacing = 16.0) {
                                    alignmentProperty().set(Pos.CENTER_LEFT)
                                    label("Выберите опыты:")
//                            checkbox("Выбрать все") {
//
//                            }
//                                    buttonSelectAll = button("Выбрать все") {
//                                        action {
//                                            if (text == "Выбрать все") {
//                                                checkBoxTest1.isSelected = true
//                                                checkBoxTest2.isSelected = true
//                                                checkBoxTest3.isSelected = true
//                                                checkBoxTest4.isSelected = true
//                                                checkBoxTest5.isSelected = true
//                                                checkBoxTest6.isSelected = true
//                                                text = "Развыбрать все"
//                                            } else {
//                                                checkBoxTest1.isSelected = false
//                                                checkBoxTest2.isSelected = false
//                                                checkBoxTest3.isSelected = false
//                                                checkBoxTest4.isSelected = false
//                                                checkBoxTest5.isSelected = false
//                                                checkBoxTest6.isSelected = false
//                                                text = "Выбрать все"
//                                            }
//                                        }
//                                    }
                                    checkBoxTest1 = checkbox("1. Сопротивление обмоток постоянному току") {}
                                    checkBoxTest2 = checkbox("2. Сопротивление изоляции") {}
                                    checkBoxTest3 = checkbox("3. Электрическая прочность изоляции") {}
                                    checkBoxTest4 = checkbox("4. Межвитковые замыкания, обрывы") {}
                                    checkBoxTest5 = checkbox("5. ???Правильность соединения обмоток???") {}
                                    checkBoxTest6 = checkbox("6. Доп.") {}
                                }
                            }
                            buttonStart = button("Запустить") {
                                prefWidth = 640.0
                                prefHeight = 128.0
                                action {
                                    controller.handleStartTest()
                                }
                            }.addClass(megaHard)
                        }
                    }
                }
                tab("Результаты") {
                    isClosable = false
                    anchorpane {
                        vbox(spacing = 8) {
                            anchorpaneConstraints {
                                leftAnchor = 16.0
                                rightAnchor = 16.0
                                topAnchor = 16.0
                                bottomAnchor = 16.0
                            }
                            alignment = Pos.CENTER

                            tableview(controller.tableValuesTest1) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest1::descriptor.getter)
                                column("R AB, Ом", TableValuesTest1::resistanceAB.getter)
                                column("R BC, Ом", TableValuesTest1::resistanceBC.getter)
                                column("R CA, Ом", TableValuesTest1::resistanceCA.getter)
                                column("Результат", TableValuesTest1::result.getter)
                            }
                            tableview(controller.tableValuesTest2) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest2::descriptor.getter)
                                column("R, Ом", TableValuesTest2::resistanceR.getter)
                                column("Результат", TableValuesTest2::result.getter)
                            }
                            tableview(controller.tableValuesTest3) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest3::descriptor.getter)
                                column("U, В", TableValuesTest3::voltage.getter)
                                column("I, мА", TableValuesTest3::current.getter)
                                column("Результат", TableValuesTest3::result.getter)
                            }
                            tableview(controller.tableValuesTest4) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest4::descriptor.getter)
                                column("R AB, Ом", TableValuesTest4::resistanceInductiveAB.getter)
                                column("R BC, Ом", TableValuesTest4::resistanceInductiveBC.getter)
                                column("R CA, Ом", TableValuesTest4::resistanceInductiveCA.getter)
                                column("Результат", TableValuesTest4::result.getter)
                            }
                            tableview(controller.tableValuesTest5) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest5::descriptor.getter)
                                column("U, В", TableValuesTest5::resistance.getter)
                                column("Результат", TableValuesTest5::result.getter)
                            }
                            tableview(controller.tableValuesTest6) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest6::descriptor.getter)
                                column("Результат", TableValuesTest6::result.getter)
                            }
                        }
                    }
                }
            }
        }
        bottom = hbox {
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
        button("Включить освещение") {
            action {
            }
        }
    }.addClass(Styles.blueTheme, megaHard)

    fun start1Test() {
        replaceWith<Test1View>(transitionLeft)
    }

    fun start2Test() {
        replaceWith<Test2View>(transitionLeft)
    }

    fun start3Test() {
        replaceWith<Test3View>(transitionLeft)
    }

    fun start4Test() {
        replaceWith<Test4View>(transitionLeft)
    }

    fun start5Test() {
        replaceWith<Test5View>(transitionLeft)
    }

    fun start6Test() {
        replaceWith<Test6View>(transitionLeft)
    }


}
