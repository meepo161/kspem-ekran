package ru.avem.ekran.view

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.ekran.controllers.MainViewController
import ru.avem.ekran.controllers.ObjectTypeEditorController
import ru.avem.ekran.database.entities.ObjectsTypes
import ru.avem.ekran.database.entities.TestObjectsType
import tornadofx.*

class ObjectTypeEditorWindow : View("Редактор объектов") {
    var textfieldPressure: TextField by singleAssign()
    var textfieldTime: TextField by singleAssign()

    var tableViewObjects: TableView<TestObjectsType> by singleAssign()

    private val controller: ObjectTypeEditorController by inject()
    private val mainController: MainViewController by inject()

    override val root = anchorpane {
        prefWidth = 1600.0
        prefHeight = 1000.0
        hbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }
            alignment = Pos.CENTER

            tableViewObjects = tableview {
                columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY)
                prefWidth = 1600.0
                items = controller.getObjects()

                column("Серийный номер", TestObjectsType::serialNumber) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.serialNumber eq selectedItem!!.serialNumber
                            }) {
                                it[serialNumber] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()

                column("Сопротивление катушек", TestObjectsType::resistanceCoil) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.resistanceCoil eq selectedItem!!.resistanceCoil
                            }) {
                                it[resistanceCoil] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Сопротивление контактных групп", TestObjectsType::resistanceContactGroup) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.resistanceContactGroup eq selectedItem!!.resistanceContactGroup
                            }) {
                                it[resistanceContactGroup] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Мин напряжение", TestObjectsType::voltageMin) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.voltageMin eq selectedItem!!.voltageMin
                            }) {
                                it[voltageMin] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Макс напряжение", TestObjectsType::voltageMax) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.voltageMax eq selectedItem!!.voltageMax
                            }) {
                                it[voltageMax] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Время", TestObjectsType::timeOff) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.timeOff eq selectedItem!!.timeOff
                            }) {
                                it[timeOff] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
            }
        }.addClass(Styles.medium)
    }.addClass(Styles.medium, Styles.blueTheme)
}
