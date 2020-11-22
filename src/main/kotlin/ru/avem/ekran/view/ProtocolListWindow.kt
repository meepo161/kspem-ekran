package ru.avem.ekran.view

import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.stage.FileChooser
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import ru.avem.ekran.database.entities.Protocol
import ru.avem.ekran.database.entities.ProtocolsTable
import ru.avem.ekran.protocol.saveProtocolAsWorkbook
import ru.avem.ekran.utils.callKeyBoard
import ru.avem.ekran.utils.openFile
import tornadofx.*
import tornadofx.controlsfx.confirmNotification
import java.io.File

class ProtocolListWindow : View("Протоколы") {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    private var tableViewProtocols: TableView<Protocol> by singleAssign()
    private lateinit var protocols: ObservableList<Protocol>
    override fun onDock() {
        protocols = transaction {
            Protocol.all().toList().sortedByDescending { it.id }.asObservable()
        }

        tableViewProtocols.items = protocols
    }

    override val root = anchorpane {
        prefWidth = 1200.0
        prefHeight = 740.0
        maxHeight = 720.0

        vbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }

            alignmentProperty().set(Pos.CENTER)

            textfield {
                callKeyBoard()
                prefWidth = 600.0

                promptText = "Фильтр"
                alignment = Pos.CENTER

                onKeyReleased = EventHandler {
                    if (!text.isNullOrEmpty()) {
                        tableViewProtocols.items = protocols.filter { it.factoryNumber.contains(text) }.asObservable()
                    } else {
                        tableViewProtocols.items = protocols
                    }
                }
            }

            tableViewProtocols = tableview {
                prefHeight = 700.0
                columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY)

                column("Изделие", Protocol::factoryNumber)
                column("Объект", Protocol::objectType)
                column("Дата", Protocol::date)
                column("Время", Protocol::dateTime)
            }

            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER)

                button("Открыть") {
                    action {
                        if (tableViewProtocols.selectedItem != null) {
                            val protocol = transaction {
                                Protocol.find {
                                    ProtocolsTable.id eq tableViewProtocols.selectedItem!!.id
                                }.toList().asObservable()
                            }.first()

                            close()
                            saveProtocolAsWorkbook(protocol)
                            openFile(File("protocol.xlsx"))
                        }
                    }
                }
                button("Сохранить как") {
                    action {
                        if (tableViewProtocols.selectedItem != null) {
                            val files = chooseFile(
                                "Выберите директорию для сохранения",
                                arrayOf(FileChooser.ExtensionFilter("XSLX Files (*.xlsx)", "*.xlsx")),
                                FileChooserMode.Save,
                                this@ProtocolListWindow.currentWindow
                            ) {
                                this.initialDirectory = File(System.getProperty("user.home"))
                            }

                            if (files.isNotEmpty()) {
                                saveProtocolAsWorkbook(tableViewProtocols.selectedItem!!, files.first().absolutePath)
                                confirmNotification(
                                    "Готово",
                                    "Успешно сохранено",
                                    Pos.BOTTOM_CENTER,
                                    owner = this@ProtocolListWindow.currentWindow
                                )
                            }
                        }
                    }
                }
                button("Сохранить все") {
                    action {
                        if (tableViewProtocols.items.size > 0) {
                            val dir = chooseDirectory(
                                "Выберите директорию для сохранения",
                                File(System.getProperty("user.home")),
                                this@ProtocolListWindow.currentWindow
                            )

                            if (dir != null) {
                                tableViewProtocols.items.forEach {
                                    val file = File(dir, "${it.id.value}.xlsx")
                                    saveProtocolAsWorkbook(it, file.absolutePath)
                                }
                                confirmNotification(
                                    "Готово",
                                    "Успешно сохранено",
                                    Pos.BOTTOM_CENTER,
                                    owner = this@ProtocolListWindow.currentWindow
                                )
                            }
                        }
                    }
                }
                button("Удалить") {
                    action {
                        if (tableViewProtocols.selectedItem != null) {
                            transaction {
                                ProtocolsTable.deleteWhere {
                                    ProtocolsTable.id eq tableViewProtocols.selectedItem!!.id
                                }
                            }

                            tableViewProtocols.items = transaction {
                                Protocol.all().toList().asObservable()
                            }
                        }
                    }
                }
            }
        }
    }.addClass(Styles.extraHard, Styles.blueTheme)
}
