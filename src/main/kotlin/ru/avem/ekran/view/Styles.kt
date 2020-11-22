package ru.avem.ekran.view

import javafx.geometry.Pos
import javafx.scene.paint.Paint
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val lineChart by cssclass()
        val pressure by cssclass()
        val blueTheme by cssclass()
        val medium by cssclass()
        val hard by cssclass()
        val extraHard by cssclass()
        val megaHard by cssclass()
        val stopStart by cssclass()
        val anchorPaneBorders by cssclass()
        val anchorPaneStatusColor by cssclass()
        val roundButton by cssclass()
        val powerButtons by cssclass()
        val kVArPowerButtons by cssclass()
        val tableRowCell by cssclass()
        val vboxTextArea by cssclass()
        val alert by cssclass()
    }

    init {
        alert {
            baseColor = c("#222")
            fontSize = 18.px
            fontWeight = FontWeight.BOLD
        }

        blueTheme {
//            baseColor = c("#0f4c81")
            baseColor = c("#444")
            accentColor = c("#f88379")
            focusColor = Paint.valueOf("#f88379")
        }

        tabPane {
            tab {
                focusColor = Paint.valueOf("#00000000") //transparent
            }
        }

        pressure {
            font = loadFont("/font/DSEG7Modern-BoldItalic.ttf", 24.0)!!
            backgroundColor += c("#2e0d08")
            textFill = c("#ff3000")
            fontWeight = FontWeight.EXTRA_BOLD
        }

        medium {
            fontSize = 18.px
            fontWeight = FontWeight.BOLD
        }

        hard {
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }

        extraHard {
            fontSize = 24.px
            fontWeight = FontWeight.EXTRA_BOLD
        }

        megaHard {
            fontSize = 32.px
            fontWeight = FontWeight.EXTRA_BOLD
        }

        stopStart {
            fontSize = 60.px
            fontWeight = FontWeight.EXTRA_BOLD
            baseColor = c("#333")
        }

        powerButtons {
            fontSize = 18.px
//            baseColor = c("#2178CC")
            baseColor = c("#222")
            prefWidth = 50.px
        }

        kVArPowerButtons {
            fontSize = 18.px
            baseColor = c("#60C3CC")
            prefWidth = 50.px
        }

        anchorPaneBorders {
            borderColor += CssBox(
                top = c("grey"),
                bottom = c("grey"),
                left = c("grey"),
                right = c("grey")
            )
        }

        anchorPaneStatusColor {
            backgroundColor += c("#B4AEBF")
        }

        roundButton {
            backgroundRadius += CssBox(
                top = 30.px,
                bottom = 30.px,
                left = 30.px,
                right = 30.px
            )
        }

        tableColumn {
            alignment = Pos.CENTER
            fontWeight = FontWeight.EXTRA_BOLD
            fontSize = 22.px
        }

        tableRowCell {
            cellSize = 50.px
        }

        checkBox {
            selected {
                mark {
                    backgroundColor += c("black")
                }
            }
        }

        vboxTextArea {
//            backgroundColor += c("#6696bd")

            backgroundColor += c("#333")
        }

        lineChart {
            chartSeriesLine {
                backgroundColor += c("red")
                stroke = c("red")
            }
            chartLineSymbol {
                backgroundColor += c("red")
            }
        }
    }
}
