package ru.avem.ekran.view

import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.stage.Modality
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.ekran.database.entities.User
import ru.avem.ekran.database.entities.Users
import ru.avem.ekran.database.entities.Users.login
import tornadofx.*
import tornadofx.controlsfx.warningNotification

class UserEditorWindow : View("Редактор пользователей") {
    private var tableViewUsers: TableView<User> by singleAssign()

    override fun onBeforeShow() {
        modalStage!!.setOnHiding {
        }
    }

    fun refreshUsersTable() {
        tableViewUsers.items = getUsers()
    }

    private fun getUsers(): ObservableList<User> {
        return transaction {
            User.all().toList().asObservable()
        }
    }

    override val root = anchorpane {
        hbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                bottomAnchor = 16.0
                topAnchor = 16.0
            }

            alignmentProperty().set(Pos.CENTER)

            tableViewUsers = tableview {
                prefWidth = 800.0

                columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY)

                items = getUsers()

                column("Логин", User::login)
                column("Пароль", User::password) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            Users.update({
                                login eq selectedItem!!.login
                            }) {
                                it[password] = cell.newValue
                            }
                        }
                    }
                }
                column("ФИО", User::fullName) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            Users.update({
                                login eq selectedItem!!.login
                            }) {
                                it[fullName] = cell.newValue
                            }
                        }
                    }
                }.makeEditable()
            }

            vbox(spacing = 16.0) {
                button("Добавить пользователя") {
                    prefWidth = 300.0

                    action {
                        find<UserAddWindow>().openModal(
                            modality = Modality.WINDOW_MODAL, escapeClosesWindow = true,
                            owner = this@UserEditorWindow.currentWindow, resizable = false
                        )
                    }
                }

                button("Удалить пользователя") {
                    prefWidth = 300.0

                    action {
                        val item = tableViewUsers.selectedItem
                        if (item != null) {
                            if (item.login == "admin") {
                                warningNotification(
                                    "Удаление пользователя",
                                    "Нельзя удалить учетную запись администратора.",
                                    Pos.BOTTOM_CENTER
                                )
                            } else {
                                confirm(
                                    "Удаление пользователя ${item.login}",
                                    "Вы действительно хотите удалить пользователя?",
                                    ButtonType.YES, ButtonType.NO,
                                    owner = this@UserEditorWindow.currentWindow,
                                    title = "Удаление пользователя ${item.login}"
                                ) {
                                    transaction {
                                        Users.deleteWhere { login eq item.login }
                                    }
                                    refreshUsersTable()
                                }
                            }
                        }
                    }
                }
            }
        }
    }.addClass(Styles.medium, Styles.blueTheme)
}
