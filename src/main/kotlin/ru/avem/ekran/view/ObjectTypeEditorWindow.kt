package ru.avem.ekran.view

import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.ekran.controllers.MainViewController
import ru.avem.ekran.controllers.ObjectTypeEditorController
import ru.avem.ekran.database.entities.ObjectsTypes
import ru.avem.ekran.database.entities.TestObjectsType
import ru.avem.ekran.utils.callKeyBoard
import tornadofx.*

class ObjectTypeEditorWindow : View("Редактор объектов") {
    var textfieldType: TextField by singleAssign()
    var textfieldXR: TextField by singleAssign()
    var textfieldXL: TextField by singleAssign()
    var textfieldXIsolation: TextField by singleAssign()

    var tableViewObjects: TableView<TestObjectsType> by singleAssign()

    private val controller: ObjectTypeEditorController by inject()
    private val mainController: MainViewController by inject()

    override val root = anchorpane {
        prefWidth = 1200.0
        prefHeight = 600.0
        vbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }
            alignment = Pos.CENTER
            hbox(spacing = 16.0) {
                tableViewObjects = tableview {
                    columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY)
                    prefWidth = 1000.0
                    prefHeight = 600.0

                    enableDirtyTracking()
                    items = controller.getObjects()

                    onEditCommit {
                        tableViewObjects.items = controller.getObjects()
                    }
                    onEditStart {
                        callKeyBoard()
                    }
                    column("Тип объекта", TestObjectsType::testType) {
                        setOnEditCommit { cell ->
                            transaction {
                                ObjectsTypes.update({
                                    ObjectsTypes.id eq selectedItem!!.id
                                }) {
                                    it[testType] = cell.newValue

                                }
                            }
                            tableViewObjects.items = controller.getObjects()
                            mainController.refreshObjectsTypes()
                        }
                        addClass(Styles.medium)
                    }.makeEditable()

                    column("XR постоянному току, Ом", TestObjectsType::xR) {
                        setOnEditCommit { cell ->
                            transaction {
                                ObjectsTypes.update({
                                    ObjectsTypes.id eq selectedItem!!.id
                                }) {
                                    it[xR] = cell.newValue
                                }
                            }
                            tableViewObjects.items = controller.getObjects()
                            mainController.refreshObjectsTypes()
                        }
                        addClass(Styles.medium)
                    }.makeEditable()
                    column("R изоляции, МОм", TestObjectsType::rIsolation) {
                        setOnEditCommit { cell ->
                            transaction {
                                ObjectsTypes.update({
                                    ObjectsTypes.id eq selectedItem!!.id
                                }) {
                                    it[rIsolation] = cell.newValue
                                }
                            }
                            tableViewObjects.items = controller.getObjects()
                            mainController.refreshObjectsTypes()
                        }
                        addClass(Styles.medium)
                    }.makeEditable()
                    column("XL индуктивности, мН", TestObjectsType::xL) {
                        setOnEditCommit { cell ->
                            transaction {
                                ObjectsTypes.update({
                                    ObjectsTypes.id eq selectedItem!!.id
                                }) {
                                    it[xL] = cell.newValue
                                }
                            }
                            tableViewObjects.items = controller.getObjects()
                            mainController.refreshObjectsTypes()
                        }
                        addClass(Styles.medium)
                    }.makeEditable()
                }

                vbox(spacing = 16.0) {
                    hbox(spacing = 4.0) {
                        vbox(spacing = 4.0) {
                            alignment = Pos.CENTER
                            label("Тип объекта")
                            textfieldType = textfield {
                                prefWidth = 300.0
                                    callKeyBoard()
                            }
                        }
                    }
                    hbox(spacing = 4.0) {
                        vbox(spacing = 4.0) {
                            alignment = Pos.CENTER
                            label("XR постоянному току, Ом")
                            textfieldXR = textfield {
                                prefWidth = 300.0
                                    callKeyBoard()
                            }
                        }
                    }
                    hbox(spacing = 4.0) {
                        vbox(spacing = 4.0) {
                            alignment = Pos.CENTER
                            label("R изоляции, МОм")
                            textfieldXIsolation = textfield {
                                prefWidth = 300.0
                                    callKeyBoard()
                            }
                        }
                    }
                    hbox(spacing = 4.0) {
                        vbox(spacing = 4.0) {
                            alignment = Pos.CENTER
                            label("XL индуктивности, мН")
                            textfieldXL = textfield {
                                prefWidth = 300.0
                                    callKeyBoard()
                            }
                        }
                    }

                    hbox(spacing = 16.0) {
                        alignment = Pos.CENTER
                        button("Добавить") {
                            action {
                                if (controller.areFieldsValid()) {
                                    transaction {
                                        TestObjectsType.new {
                                            testType = textfieldType.text.toString()
                                            xR = textfieldXR.text.toString().replace(',', '.')
                                            rIsolation = textfieldXIsolation.text.toString().replace(',', '.')
                                            xL = textfieldXL.text.toString().replace(',', '.')
                                        }
                                    }
                                    tableViewObjects.items = controller.getObjects()
                                    mainController.refreshObjectsTypes()
                                }
                            }
                        }
                        button("Удалить") {
                            action {
                                confirm(
                                    "Удаление ОИ ${tableViewObjects.selectedItem!!.id}",
                                    "Вы действительно хотите удалить объект испытания?",
                                    ButtonType("ДА"), ButtonType("НЕТ"),
                                    owner = this@ObjectTypeEditorWindow.currentWindow,
                                    title = "Удаление ОИ ${tableViewObjects.selectedItem!!.id}"
                                ) {
                                    with(tableViewObjects.selectedItem!!) {
                                        transaction {
                                            ObjectsTypes.deleteWhere { ObjectsTypes.id eq id }
                                        }
                                    }
                                    tableViewObjects.items = controller.getObjects()
                                    mainController.refreshObjectsTypes()
                                }
                            }
                        }
                    }
                }

            }.addClass(Styles.medium)
        }
    }.addClass(Styles.medium, Styles.blueTheme)
}
