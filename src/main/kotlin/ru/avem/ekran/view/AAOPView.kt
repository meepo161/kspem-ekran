package ru.avem.ekran.view

import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.layout.VBox
import ru.avem.ekran.communication.adapters.ack3002.driver.AAOPController
import tornadofx.*

class AAOPView : View("Осцилограф") {
    private val controller = AAOPController.also {
        it.view = this
    }

    var layoutBack: VBox by singleAssign()
    var ctrlLayout: VBox by singleAssign()
    var trglevelLayout: VBox by singleAssign()
    var ofs1Layout: VBox by singleAssign()
    var ofs2Layout: VBox by singleAssign()
    var btnClose: Button by singleAssign()
    var btnSave: Button by singleAssign()
    var btnLoad: Button by singleAssign()
    var autosetBtn: Button by singleAssign()
    var btnHelp: Button by singleAssign()
    var btnOur: Button by singleAssign()
    var channel1Lbl: Label by singleAssign()
    var range1Spinner: ComboBox<String> by singleAssign()
    var cpl1Spinner: ComboBox<String> by singleAssign()
    var probe1Spinner: ComboBox<String> by singleAssign()
    var channel2Lbl: Label by singleAssign()
    var range2Spinner: ComboBox<String> by singleAssign()
    var cpl2Spinner: ComboBox<String> by singleAssign()
    var probe2Spinner: ComboBox<String> by singleAssign()
    var tbSpinner: ComboBox<String> by singleAssign()
    var dataLenSpinner: ComboBox<String> by singleAssign()
    var pretrgSeekBar: Slider by singleAssign()
    var ctrlShowBtn: Button by singleAssign()
    var freq1Txt: Label by singleAssign()
    var amps1Txt: Label by singleAssign()
    var ampp1Txt: Label by singleAssign()
    var freq2Txt: Label by singleAssign()
    var amps2Txt: Label by singleAssign()
    var ampp2Txt: Label by singleAssign()
    var runBtn: ToggleButton by singleAssign()
    var runmodeSpinner: ComboBox<String> by singleAssign()
    var trgsrcSpinner: ComboBox<String> by singleAssign()
    var trglogSpinner: ComboBox<String> by singleAssign()
    var generatorSpinner: ComboBox<String> by singleAssign()
    var tvScale: Label by singleAssign()
    var trglevelSeekBar: Slider by singleAssign()
    var plotCanvas: Canvas by singleAssign()
    var offset1SeekBar: Slider by singleAssign()
    var offset2SeekBar: Slider by singleAssign()

    private var tbList = observableListOf(
        "1 кГц",
        "2 кГц",
        "5 кГц",
        "10 кГц",
        "20 кГц",
        "50 кГц",
        "100 кГц",
        "200 кГц",
        "500 кГц",
        "1 МГц",
        "2 МГц",
        "5 МГц",
        "10 МГц",
        "20 МГц",
        "50 МГц",
        "100 МГц"
    )

    private var lenList = observableListOf("1K т", "10K т", "100K т")

    private var rangeList = observableListOf(
        "10 мВ/дел",
        "20 мВ/дел",
        "50 мВ/дел",
        "100 мВ/дел",
        "200 мВ/дел",
        "500 мВ/дел",
        "1 В/дел",
        "2 В/дел",
        "5 В/дел",
        "10 В/дел"
    )

    private var cplList = observableListOf("DC", "AC", "Gnd", "50 Ω", "Выкл")

    private var trglogList = observableListOf("T↑", "T↓")

    private var probeList = observableListOf("1:1", "1:10", "1:100")

    private var runmodeList = observableListOf("Авто", "Норм", "Один")

    private var trgsrcList = observableListOf("1", "2", "Ext")

    private var generatorList = observableListOf("____", "_┌┐_")

    var prefixList = observableListOf("п", "н", "мк", "м", "", "к", "М")

    override fun onDock() {
        super.onDock()
        controller.onCreate()
        currentWindow?.setOnCloseRequest { controller.onDestroy() }
    }

    override val root = anchorpane {
        layoutBack = vbox(spacing = 32.0) {
            alignmentProperty().set(Pos.CENTER)

            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }

            hbox(spacing = 8.0) {
                alignmentProperty().set(Pos.CENTER)

                btnClose = button("Close") {
                    prefWidth = 100.0
                    action {
                        controller.handleBtnClose()
                    }
                }

                btnSave = button("Save") {
                    prefWidth = 100.0
                    action {
                        println("handleBtnSave()")
                    }
                }

                btnLoad = button("Load") {
                    prefWidth = 100.0
                    action {
                        controller.handleBtnOur()
                    }
                }

                autosetBtn = button("autoSet") {
                    prefWidth = 100.0
                    action {
                        controller.handleBtnAutoSet()
                    }
                }

                btnHelp = button("Help") {
                    prefWidth = 100.0
                    action {
                        controller.handleBtnHelp()
                    }
                }

                btnOur = button("Our") {
                    prefWidth = 100.0
                    action {
                        controller.handleBtnOur()
                    }
                }
            }

            vbox(spacing = 8.0) {
                alignmentProperty().set(Pos.CENTER)

                hbox(spacing = 8.0) {
                    alignmentProperty().set(Pos.CENTER)
                    channel1Lbl = label("1")
                    range1Spinner = combobox {
                        prefWidth = 200.0
                        items = rangeList
                    }
                    cpl1Spinner = combobox {
                        prefWidth = 200.0
                        items = cplList
                    }
                    probe1Spinner = combobox {
                        prefWidth = 200.0
                        items = probeList
                    }
                }

                hbox(spacing = 8.0) {
                    alignmentProperty().set(Pos.CENTER)

                    channel2Lbl = label("2")

                    range2Spinner = combobox {
                        prefWidth = 200.0
                        items = rangeList
                    }

                    cpl2Spinner = combobox {
                        prefWidth = 200.0
                        items = cplList
                    }

                    probe2Spinner = combobox {
                        prefWidth = 200.0
                        items = probeList
                    }
                }

                hbox(spacing = 8.0) {
                    alignmentProperty().set(Pos.CENTER)

                    tbSpinner = combobox {
                        items = tbList
                        prefWidth = 200.0
                    }

                    dataLenSpinner = combobox {
                        prefWidth = 200.0
                        items = lenList
                    }

                    pretrgSeekBar = slider {

                    }
                }
            }

            hbox(spacing = 8.0) {
                alignmentProperty().set(Pos.CENTER)

                ctrlShowBtn = button("Control") {
                    prefWidth = 600.0
                    action {
                        controller.handleRunBtn()
                    }
                }
            }

            hbox(spacing = 8.0) {
                alignmentProperty().set(Pos.CENTER)

                vbox(spacing = 8.0) {
                    alignmentProperty().set(Pos.CENTER)
                    freq1Txt = label("f1: --- Hz")
                    amps1Txt = label("a1(sine): --- V")
                    ampp1Txt = label("a1(puls): --- V")
                }

                vbox(spacing = 8.0) {
                    alignmentProperty().set(Pos.CENTER)
                    freq2Txt = label("f2: --- Hz")
                    amps2Txt = label("a2(sine): --- V")
                    ampp2Txt = label("a2(puls): --- V")
                }
            }

            hbox(spacing = 8.0) {
                alignmentProperty().set(Pos.CENTER)

                runBtn = togglebutton("Run") {
                    prefWidth = 140.0
                }

                runmodeSpinner = combobox {
                    prefWidth = 140.0
                    items = runmodeList
                }

                trgsrcSpinner = combobox {
                    prefWidth = 140.0
                    items = trgsrcList
                }

                trglogSpinner = combobox {
                    prefWidth = 140.0
                    items = trglogList
                }

                generatorSpinner = combobox {
                    prefWidth = 140.0
                    items = generatorList
                }
            }

            hbox(spacing = 8.0) {
                alignmentProperty().set(Pos.CENTER)
                tvScale = label("T: 10 µs/d; 1: 1 V/d; 2: 1 V/d")
            }

            hbox(spacing = 8.0) {
                alignmentProperty().set(Pos.CENTER)

                trglevelLayout = vbox(spacing = 8.0) {
                    alignmentProperty().set(Pos.CENTER)
                    label("T")
                    trglevelSeekBar = slider {
                        orientation = Orientation.VERTICAL
                    }
                }

                plotCanvas = canvas(600.0, 600.0)

                ofs1Layout = vbox(spacing = 8.0) {
                    alignmentProperty().set(Pos.CENTER)
                    label("1")
                    offset1SeekBar = slider {
                        orientation = Orientation.VERTICAL
                    }
                }

                ofs2Layout = vbox(spacing = 8.0) {
                    alignmentProperty().set(Pos.CENTER)
                    label("2")
                    offset2SeekBar = slider {
                        orientation = Orientation.VERTICAL
                    }
                }
            }
        }
    }

}
