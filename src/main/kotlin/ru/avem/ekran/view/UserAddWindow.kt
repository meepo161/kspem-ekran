package ru.avem.ekran.view

import javafx.geometry.Pos
import javafx.scene.control.TextField
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.ekran.database.entities.User
import ru.avem.ekran.utils.callKeyBoard
import tornadofx.*
import tornadofx.controlsfx.warningNotification

class UserAddWindow : View("Добавить пользователя") {
    private val parentView: UserEditorWindow by inject()

    private var textFieldLogin: TextField by singleAssign()
    private var textFieldPassword: TextField by singleAssign()
    private var textFieldFullName: TextField by singleAssign()

    override fun onBeforeShow() {
        modalStage!!.setOnHiding {
            parentView.refreshUsersTable()
        }
    }

    override val root = anchorpane {
        vbox(spacing = 16.0) {
            prefWidth = 300.0

            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }

            alignmentProperty().set(Pos.CENTER)

            hbox(spacing = 25.0) {
                alignmentProperty().set(Pos.CENTER_RIGHT)

                label("Логин")
                textFieldLogin = textfield {
                    prefWidth = 200.0
                    callKeyBoard()

                }
            }

            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER_RIGHT)

                label("Пароль")
                textFieldPassword = textfield {
                    prefWidth = 200.0

                    callKeyBoard()

                }
            }

            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER_RIGHT)

                label("ФИО")
                textFieldFullName = textfield {
                    prefWidth = 200.0

                    callKeyBoard()

                }
            }



            button("Добавить") {
                action {
                    val userLogin = textFieldLogin.text
                    val userPassword = textFieldPassword.text
                    val fullName = textFieldFullName.text

                    if (userLogin.isNullOrEmpty() or userPassword.isNullOrEmpty() or fullName.isNullOrEmpty()) {
                        warningNotification(
                            "Заполнение полей",
                            "Заполните все поля и повторите снова.",
                            Pos.BOTTOM_CENTER
                        )
                    } else {
                        transaction {
                            User.new {
                                login = userLogin
                                password = userPassword
                                this.fullName = fullName
                            }
                        }
                        parentView.refreshUsersTable()
                        textFieldFullName.clear()
                        textFieldLogin.clear()
                        textFieldPassword.clear()
                        this@UserAddWindow.close()
                    }
                }
            }
        }
    }.addClass(Styles.medium, Styles.blueTheme)
}
