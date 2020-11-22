package ru.avem.ekran.view

import tornadofx.View
import tornadofx.addClass
import tornadofx.anchorpane
import tornadofx.label

class DevicesView : View("Связь с приборами") {

    override val root = anchorpane {
        prefWidth = 1280.0
        prefHeight = 720.0
        
        label("Данная функция временно недоступна")
    }.addClass(Styles.blueTheme, Styles.megaHard)
}