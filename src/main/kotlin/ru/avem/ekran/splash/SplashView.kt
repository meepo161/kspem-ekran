package ru.avem.ekran.splash

import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Modality
import org.slf4j.LoggerFactory
import ru.avem.ekran.view.AuthorizationView
import ru.avem.ekran.view.Styles
import tornadofx.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class SplashView : View("splash") {
    companion object {
        private val logger = LoggerFactory.getLogger(SplashView::class.java)
    }

    private var img: ImageView by singleAssign()


    override fun onDock() {
        runAsync {
        } ui {
            thread(isDaemon = true) {
                sleep(2000L)
                runLater {
                    openNextWindow()
                }
            }
        }
    }

    private fun openNextWindow() {
        close()
        find<AuthorizationView>().openWindow(
            modality = Modality.APPLICATION_MODAL, escapeClosesWindow = true,
            resizable = false, owner = this.currentWindow
        )
    }

    override val root =
        anchorpane {
            img = imageview(Image("splash.png"))
            maxWidth = 1280.0
            maxHeight = 800.0
            minWidth = 1280.0
            minHeight = 800.0
            vbox(spacing = 32.0) {
                anchorpaneConstraints {
                    leftAnchor = 16.0
                    rightAnchor = 16.0
                    topAnchor = 16.0
                    bottomAnchor = 16.0
                }
                alignmentProperty().set(Pos.CENTER)
                label("Загрузка...") {

                }.addClass(Styles.stopStart)
            }
        }.addClass(Styles.blueTheme)
}
