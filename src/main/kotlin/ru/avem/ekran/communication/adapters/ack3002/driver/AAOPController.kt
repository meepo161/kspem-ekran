package ru.avem.ekran.communication.adapters.ack3002.driver

import javafx.application.Platform
import javafx.collections.FXCollections
import ru.avem.ekran.communication.adapters.ack3002.driver.Utils.IAULNetListenerIACKScopeListener
import ru.avem.ekran.communication.adapters.ack3002.driver.Utils.nextIntFall
import ru.avem.ekran.communication.adapters.ack3002.driver.Utils.nextIntRise
import ru.avem.ekran.utils.sleep
import ru.avem.ekran.view.AAOPView
import tornadofx.c
import tornadofx.runLater
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.sin

object AAOPController : IAULNetListenerIACKScopeListener {
    lateinit var view: AAOPView

    private var autoControl = 0
    private var ch1Enabled = true
    private var ch2Enabled = true

    var dataLenK = 1
    private var xvalues = FloatArray(dataLenK * 1024)
    private val yvalues = arrayOfNulls<FloatArray>(2)

    var dataWaiting = 0
    var fontSize = 0f

    private var noVoidData = false

    private var pACKScopeDrv: ACKScopeDrv? = null
    private val pltColors = arrayOf(
        c("FF0000"), c("0000FF"), c("008000"), c("FF00FF"), c("008080"), c("FF4000"), c("808000"), c("700080")
    )
    private val prefixMultipliers = floatArrayOf(1.0E12f, 1.0E9f, 1000000.0f, 1000.0f, 1.0f, 0.001f, 1.0E-6f)
    private val prefixStrings = arrayOf("p", "n", "µ", "m", "<", "K", "M")

    //    private val prefixStrings = view.prefixList
    private var pretrgRate = 0.5

    private var plot2d: Plot2d? = null

    private var mHandler: ExecutorService? = null

    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            when (dataWaiting) {
                0 -> {
                    dataWaiting = 1
                    pACKScopeDrv?.readWaveform()
                }
                2, 3, 4 -> {
                    plot2d!!.plots[0]!!.visible = ch1Enabled
                    val vectorOffset = dataLenK * 12
                    if (ch1Enabled) {
                        plot2d!!.setData(0, xvalues, yvalues[0]!!, vectorOffset, xvalues.size - vectorOffset * 2)
                    }

                    plot2d!!.plots[1]!!.visible = ch2Enabled
                    if (ch2Enabled) {
                        plot2d!!.setData(1, xvalues, yvalues[1]!!, vectorOffset, xvalues.size - vectorOffset * 2)
                    }

                    if (ch1Enabled || ch2Enabled) {
                        plot2d!!.onDraw()
                    }

                    plot2d!!.xmarks[0]!!.x = (-100.0 + 200.0 * pretrgRate).toFloat()
                    doMeas()
                    if (dataWaiting != 3 && pACKScopeDrv!!.getTriggerMode() != 2) {
                        dataWaiting = 1
                        pACKScopeDrv!!.readWaveform()
                    } else {
                        dataWaiting = 0
                        startMTimer(false)
                    }
                }
            }

            sleep(500)
            mHandler?.execute(this)
        }
    }

    fun onCreate() {
        viewInitialization()
        initPlot()
        pACKScopeDrv = ACKScopeDrv(this)
        initConnection()
        updateTask.run()
    }

    private fun viewInitialization() {
        view.offset1SeekBar.max = 4095.0
        view.offset1SeekBar.valueProperty().addListener { _, _, newVal ->
            pACKScopeDrv?.setOffset(newVal.toDouble().toInt(), 0)
        }

        view.offset2SeekBar.max = 4095.0
        view.offset2SeekBar.valueProperty().addListener { _, _, newVal ->
            pACKScopeDrv?.setOffset(newVal.toDouble().toInt(), 1)
        }

        view.trglevelSeekBar.max = 4095.0
        view.trglevelSeekBar.valueProperty().addListener { _, _, newVal ->
            pACKScopeDrv?.setTriggerLevel(newVal.toDouble().toInt(), 0);
            pACKScopeDrv?.setTriggerLevel(newVal.toDouble().toInt(), 1);
        }

        view.pretrgSeekBar.max = 1023.0
        view.pretrgSeekBar.valueProperty().addListener { _, _, newVal ->
            pretrgRate = newVal as Double / view.pretrgSeekBar.max
            val newValForKotlin = (0.5 + (dataLenK * 1024).toDouble() * pretrgRate).toInt()
            pACKScopeDrv?.setTrgDelay(newValForKotlin)
            pACKScopeDrv?.setPostTrgLength(dataLenK * 1024 - newValForKotlin)
        }

        view.range1Spinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index: Int = view.range1Spinner.selectionModel.selectedIndex
            pACKScopeDrv?.setRange(index, 0)
        }
        view.range2Spinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index: Int = view.range2Spinner.selectionModel.selectedIndex
            pACKScopeDrv?.setRange(index, 1)
        }
        view.tbSpinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index: Int = view.tbSpinner.selectionModel.selectedIndex
            pACKScopeDrv?.mSampleRate = view.tbSpinner.items.size - 1 - index
        }
        view.dataLenSpinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            var index: Int = view.dataLenSpinner.selectionModel.selectedIndex
            when (index) {
                0 -> dataLenK = 1
                1 -> dataLenK = 10
                2 -> dataLenK = 100
            }
            pACKScopeDrv?.let {
                if (dataLenK * 1024 > it.memorySize) dataLenK = 50
            }
            index = (0.5 + (dataLenK * 1024).toDouble() * pretrgRate).toInt()
            pACKScopeDrv?.setTrgDelay(index)
            pACKScopeDrv?.setPostTrgLength(dataLenK * 1024 - index)
        }
        view.runmodeSpinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index: Int = view.runmodeSpinner.selectionModel.selectedIndex
            pACKScopeDrv?.setTriggerMode(index)

            if (mHandler == null) {
                startMTimer(view.runBtn.isSelected)
            }
        }
        view.trgsrcSpinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index: Int = view.trgsrcSpinner.selectionModel.selectedIndex
            pACKScopeDrv?.setTriggerSource(index)
        }
        view.trglogSpinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index: Int = view.trglogSpinner.selectionModel.selectedIndex
            pACKScopeDrv?.setTriggerLogic(index)
        }
        view.generatorSpinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index: Int = view.generatorSpinner.selectionModel.selectedIndex
            pACKScopeDrv?.setGenerator(index)
        }
        view.cpl1Spinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index: Int = view.cpl1Spinner.selectionModel.selectedIndex
            ch1Enabled = index < 4
            if (ch1Enabled) {
                pACKScopeDrv?.setCoupling(index, 0)
            }
        }
        view.cpl2Spinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index = view.cpl2Spinner.selectionModel.selectedIndex
            ch2Enabled = index < 4
            if (ch2Enabled) {
                pACKScopeDrv?.setCoupling(index, 1)
            }
        }
        view.probe1Spinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index: Int = view.probe1Spinner.selectionModel.selectedIndex
            dataLenK = when (index) {
                0 -> 1
                1 -> 10
                2 -> 100
                else -> 1
            }
            pACKScopeDrv?.setProbe(dataLenK, 0)
        }
        view.probe2Spinner.valueProperty().addListener { observableValue, oldVal, newVal ->
            val index: Int = view.probe2Spinner.selectionModel.selectedIndex
            dataLenK = when (index) {
                0 -> 1
                1 -> 10
                2 -> 100
                else -> 1
            }
            pACKScopeDrv?.setProbe(dataLenK, 1)
        }
    }

    private fun initPlot() {
        var channel = 0
        while (channel < 2) {
            yvalues[channel] = FloatArray(dataLenK * 1024)
            ++channel
        }
        channel = 0
        while (channel < xvalues.size) {
            xvalues[channel] = (0.001 * (channel + 1 - xvalues.size).toDouble()).toFloat()
            ++channel
        }
        plot2d = Plot2d(2, 1024, 0, view.plotCanvas.graphicsContext2D)
        plot2d!!.backColor = c(255, 255, 255)
        plot2d!!.axisColor = c(64, 64, 64)
        plot2d!!.setStrokeWidth(2.0f * fontSize)
        plot2d!!.setXAxis(-100.0f, 100.0f, 0.0f, 20.0f, false)
        plot2d!!.setYAxis(-100.0f, 100.0f, 0.0f, 25.0f, false)
        plot2d!!.setXAutorange(200.0f, true)
        plot2d!!.setYAutorange(200.0f, true)
        channel = 0
        while (channel < 2) { // отрисовка графиков демо-режима
            plot2d!!.plots[channel]!!.color = pltColors[channel]
            val plot: Plot2d.Plot = plot2d!!.plots[channel]!!
            plot.visible = true
            for (numOfDots in plot2d!!.plots[channel]!!.xvalues.indices) {
                plot2d!!.plots[channel]!!.xvalues[numOfDots] = (-100.0 + 200.0 * numOfDots.toDouble() / plot2d!!.plots[channel]!!.xvalues.size.toDouble()).toFloat()
                if (channel == 0) {
                    plot2d!!.plots[channel]!!.yvalues[numOfDots] = (50.0 + 20.0 * sin(0.08 * plot2d!!.plots[channel]!!.xvalues[numOfDots].toDouble())).toFloat()
                } else {
                    val yvalues: FloatArray = plot2d!!.plots[channel]!!.yvalues
                    val meanderValue: Byte = if ((numOfDots / 23 and 1) == 0) {
                        -20
                    } else {
                        -80
                    }
                    yvalues[numOfDots] = meanderValue.toFloat()
                }
            }
            ++channel
        }
        plot2d!!.xmarks[0]!!.visible = true
        plot2d!!.xmarks[0]!!.label = "T"

//        thread(isDaemon = true) {
//            while (true) {
//                plot2d!!.onDraw()
//                sleep(16)
//            }
//        }
    }

    private fun initConnection() {
        startMTimer(false)
        val longName = """
             AKTAKOM Oscilloscope Pro
             Идёт поиск…
             """.trimIndent()
        pACKScopeDrv?.initConnection() ?: error("Вызвали onCreate перед инициализацией")
    }

    private fun startMTimer(isNeedRun: Boolean) {
        println("AOPA" + " StartMTimer " + isNeedRun + " [runBtn.isChecked " + view.runBtn.isSelected + "]")

        if (mHandler != null) {
            mHandler!!.shutdown()
            mHandler = null
        }

        if (isNeedRun != view.runBtn.isSelected) {
            view.runBtn.isSelected = isNeedRun
        }

        if (isNeedRun) {
            updateTask.run()
            mHandler = Executors.newFixedThreadPool(1)
            mHandler!!.execute(updateTask)
        }
    }

    private fun autoControl(imageAutoSet: Int) {
        autoControl = if (imageAutoSet < 0) {
            val autoControlByte: Byte = if (autoControl != 0) {
                0
            } else {
                10
            }
            autoControlByte.toInt()
        } else {
            imageAutoSet
        }
    }

    private fun channelCodeToValue(var1: Int, var2: Int): Float {
        return (100.0 * (var2.toDouble() - 127.5) / 127.5).toFloat()
    }

    private fun correctOffset(_var1: Int, var2: Double, var4: Double, var6: Int) {
        var var1 = _var1
        var valueSlider: Double
        valueSlider = if (var1 != 0) {
            view.offset2SeekBar.value
        } else {
            view.offset1SeekBar.value
        }
        valueSlider += (0.0025 * (var2 + var4) * 4095.0).toInt()
        if (var1 != 0) {
            view.offset2SeekBar.value = valueSlider
        } else {
            view.offset1SeekBar.value = valueSlider
        }

        pACKScopeDrv?.setOffset(valueSlider.toInt(), var1)
        if (var6 == var1) {
            var1 = ((0.5 - 0.005 * var2) * 4095.0).toInt()
            view.trglevelSeekBar.value = var1.toDouble()
            pACKScopeDrv?.setTriggerLevel(var1, 0)
            pACKScopeDrv?.setTriggerLevel(var1, 1)
        }
    }

    private fun correctRange(channel: Int, spinnerPosition: Double) {
        var var2 = spinnerPosition
        var var5: Int = if (channel != 0) {
            view.range2Spinner.selectionModel.selectedIndex
        } else {
            view.range1Spinner.selectionModel.selectedIndex
        }
        var newSpinnerPosition: Int
        if (var2 > 0.0) {
            while (true) {
                newSpinnerPosition = var5
                if (var5 <= 0) {
                    break
                }
                if (var2 <= 2.15) {
                    newSpinnerPosition = var5
                    break
                }
                --var5
                var2 /= 2.15
            }
        } else {
            ++var5
            newSpinnerPosition = var5
            if (var5 > ACKScopeDrv.voltrangTab.size - 1) {
                newSpinnerPosition = ACKScopeDrv.voltrangTab.size - 1
            }
        }
        runLater {
            if (channel != 0) {
                view.range2Spinner.selectionModel.select(newSpinnerPosition)
            } else {
                view.range1Spinner.selectionModel.select(newSpinnerPosition)
            }
        }
        pACKScopeDrv?.setRange(newSpinnerPosition, channel)
    }

    private fun correctSampleRate(_var1: Double) {
        var var1 = _var1
        var var4: Int = if (view.tbSpinner.selectionModel.selectedIndex == -1) {
            view.tbSpinner.items.size - 1
        } else {
            view.tbSpinner.items.size - 1 - view.tbSpinner.selectionModel.selectedIndex
        }
        var var3: Int
        if (var1 > 1.0) {
            while (true) {
                var3 = var4
                if (var4 <= 0) {
                    break
                }
                if (var1 <= 2.15) {
                    var3 = var4
                    break
                }
                --var4
                var1 /= 2.15
            }
        } else {
            var3 = var4
            if (var1 > 0.0) {
                while (true) {
                    var3 = var4
                    if (var4 >= ACKScopeDrv.timebaseTab.size - 1) {
                        break
                    }
                    var3 = var4
                    if (var1 >= 0.46511627906976744) {
                        break
                    }
                    ++var4
                    var1 *= 2.15
                }
            }
        }
        runLater {
            view.tbSpinner.selectionModel.select(view.tbSpinner.items.size - 1 - var3)
        }
        pACKScopeDrv?.mSampleRate = var3
    }

    private fun setDefaultScopeSettings() {
        view.tbSpinner.selectionModel.select(ACKScopeDrv.timebaseTab.size - 1)
        view.dataLenSpinner.selectionModel.select(0)
        view.runmodeSpinner.selectionModel.select(0)
        view.trgsrcSpinner.selectionModel.select(0)
        view.trglogSpinner.selectionModel.select(0)
        view.generatorSpinner.selectionModel.select(0)
        view.range1Spinner.selectionModel.select(ACKScopeDrv.voltrangTab.size - 1)
        view.range2Spinner.selectionModel.select(ACKScopeDrv.voltrangTab.size - 1)
        view.cpl1Spinner.selectionModel.select(0)
        view.cpl2Spinner.selectionModel.select(0)
        view.probe1Spinner.selectionModel.select(0)
        view.probe2Spinner.selectionModel.select(0)
        pretrgRate = 0.5
        view.pretrgSeekBar.value = pretrgRate * view.pretrgSeekBar.max
        val var1 = ((dataLenK * 1024).toDouble() * pretrgRate + 0.5).toInt()
        pACKScopeDrv?.setTrgDelay(var1)
        pACKScopeDrv?.setPostTrgLength(dataLenK * 1024 - var1)
        view.offset1SeekBar.value = 2047.0
        view.offset2SeekBar.value = 2047.0
        view.trglevelSeekBar.value = 2047.0
        pACKScopeDrv?.setTriggerLevel(2047, 0)
        pACKScopeDrv?.setTriggerLevel(2047, 1)
    }

    private fun getFileNames(): Array<String?> {
        val file = File("/AOPA/" + "//")
        try {
            file.mkdirs()
        } catch (var3: SecurityException) {
            println("AOPAunable to write on the sd card $var3")
        }
        val fileName: Array<String?>
        if (file.exists()) {
            fileName = file.list { var1, var2 ->
                var var1 = var1
                var1 = File(var1, var2)
                var2.contains(".csv") && var1.length() > 0L
            }
        } else {
            fileName = arrayOfNulls(0)
        }
        println("AOPA" + "Files in list: " + fileName.size)
        return fileName
    }

    private fun getGraphCodeToValue(var1: Int, var2: Float): Float {
        val var5 = pACKScopeDrv?.getProbe(var1) ?: 1
        val var3 = var5.toDouble() * ACKScopeDrv.voltrangTab[pACKScopeDrv?.getRange(var1) ?: 0] / 25.0
        return (var2.toDouble() * var3).toFloat()
    }

    private fun getGraphTimeToValue(var2: Float): Float {
        val var3 = ACKScopeDrv.timebaseTab[pACKScopeDrv?.mSampleRate ?: 0]
        return (var2.toDouble() * var3).toFloat()
    }

    private fun getPrefixFormat(value: Float, var2: Int): String {
        val absValue = abs(value)
        val idx: Int
        idx = when {
            absValue < 1.0E-9 -> {
                0
            }
            absValue < 1.0E-6 -> {
                1
            }
            absValue < 0.001 -> {
                2
            }
            absValue < 1.0f -> {
                3
            }
            absValue < 1000.0 -> {
                4
            }
            absValue < 1000000.0 -> {
                5
            }
            else -> {
                6
            }
        }
        return if (var2 <= 0) {
            (prefixMultipliers[idx] * value).toString() + " " + prefixStrings[idx]
        } else {
            val numberFormat = NumberFormat.getInstance()
            var pattern = "@"
            for (i in 1 until var2) {
                pattern = "$pattern#"
            }
            (numberFormat as DecimalFormat).applyPattern(pattern)
            numberFormat.format(prefixMultipliers[idx] * value.toDouble()) + " " + prefixStrings[idx]
        }
    }

    private fun readUTFLine(var1: RandomAccessFile, var2: Int, var3: Int): String? {
        var var7: String? = null
        val var8 = ByteArray(256)
        var var4 = 0
        var var5: Int
        do {
            var5 = try {
                var1.read()
            } catch (var10: IOException) {
                var10.printStackTrace()
                -1
            }
            if (var5 != -1) {
                val var6 = var4 + 1
                var8[var4] = var5.toByte()
                var4 = var6
            }
            if (var4 > 0 && var8[var4 - 1].toInt() == 10) {
                break
            }
            if (var4 >= 256) {
                println("AOPA" + "Слишком длинная строка в файле")
                break
            }
        } while (var5 != -1)
        var var11 = var7
        if (var4 > 0) {
            var11 = String(var8, 0, var4)
        }
        var7 = var11
        if (var11 != null) {
            var7 = var11
            if (var2 > 0) {
                val var12 = var11.substring(1, var11.length - 3).split("\",\"").toTypedArray()
                if (var12.size >= var2) {
                    return var12[var3]
                }
                var7 = ""
            }
        }
        return var7
    }

    private fun valueToChannelCode(var1: Int, var2: Float): Int {
        return (0.5 + 127.5 * (1.0 + var2.toDouble() / 100.0)).toInt()
    }

    private fun writeBOM(var1: RandomAccessFile) {
        try {
            var1.write(byteArrayOf(-17, -69, -65))
        } catch (var2: IOException) {
            var2.printStackTrace()
        }
    }

    private fun doAutoControl(var1: Int, var2: Int, var3: Int, var4: Int, var5: Int, _var6: Int) {
        var var6 = _var6
        val var12: Boolean = ch1Enabled && ch2Enabled
        if (autoControl > 0) {
            var var7 = 1.0
            if (var12) {
                var7 = 0.5
            }
            val var11 = pACKScopeDrv?.mTriggerSource ?: 0
            if (var11 != 1) {
                var6 = var3
            }
            var var9: Double
            if (var6 != 0) {
                var9 = dataLenK.toDouble() * 1024.0 / var6.toDouble()
                if (var9 < 2.0 || var9 > 6.0) {
                    correctSampleRate(0.25 * var9)
                }
            }
            if (ch1Enabled) {
                val var15: Boolean = (var12 || var1 <= -99) && var2 >= 99
                var9 = -0.5 * (var2 + var1).toDouble()
                val var16: Byte = if (var12) {
                    50
                } else {
                    0
                }
                correctOffset(0, var9, var16.toDouble(), var11)
                if (var15 || Math.abs(var9) < 200.0 / 4.0) {
                    var9 = 1.0 + 1.05 * (var2 - var1).toDouble()
                    if (var9 < var7 * 200.0 / 2.5) {
                        correctRange(0, var7 * 200.0 / var9)
                    } else if (var9 > var7 * 200.0) {
                        correctRange(0, -1.0)
                    }
                }
            }
            if (ch2Enabled) {
                val var13: Boolean = var4 <= -99 && (var12 || var5 >= 99)
                var9 = -0.5 * (var5 + var4).toDouble()
                val var14: Byte = if (var12) {
                    -50
                } else {
                    0
                }
                correctOffset(1, var9, var14.toDouble(), var11)
                if (var13 || Math.abs(var9) < 200.0 / 4.0) {
                    var9 = 1.0 + 1.05 * (var5 - var4).toDouble()
                    if (var9 < var7 * 200.0 / 2.5) {
                        correctRange(1, var7 * 200.0 / var9)
                    } else if (var9 > var7 * 200.0) {
                        correctRange(1, -1.0)
                    }
                }
            }
            --autoControl
            if (autoControl == 0) {
                autoControl(0)
            }
        }
    }

    fun doMeas() {
        var var7 = 0.0
        var var11 = 0.0
        var var17 = 0.0
        var var15 = 0.0
        var var9 = 0.0
        var var13 = 0.0
        val var26 = arrayOfNulls<FloatArray>(200)
        println("AOPA" + "doMeas start...")
        var var21: Int
        var21 = 0
        while (var21 < 200) {
            var26[var21] = FloatArray(2)
            var26[var21]!![0] = 0f
            var26[var21]!![1] = 0f
            ++var21
        }

//        var21 = plot2d.getVectorLength();
        runLater {
            view.tvScale.text = "[T]: " + getPrefixFormat(
                getGraphTimeToValue((0.1 * var21.toDouble()).toFloat()), 3
            ) + "s/d; [1]: " + getPrefixFormat(getGraphCodeToValue(0, 25.0f), 3) + "V/d; [2]: " + getPrefixFormat(
                getGraphCodeToValue(
                    1, 25.0f
                ), 3
            ) + "V/d"
        }
        var var1: Double
        var var3: Double
        var var5: Double
        var var19: Double
        var var22: Int
        var var23: Int
        var var24: Int
        var var25: Int
        var var27: FloatArray?
        if (ch1Enabled && yvalues[0]!!.isNotEmpty()) {
            var5 = yvalues[0]!![0].toDouble()
            var3 = var5
            var21 = 0
            while (var21 < yvalues[0]!!.size) {
                var1 = var3
                if (var3 < yvalues[0]!![var21].toDouble()) {
                    var1 = yvalues[0]!![var21].toDouble()
                }
                var7 = var5
                if (var5 > yvalues[0]!![var21].toDouble()) {
                    var7 = yvalues[0]!![var21].toDouble()
                }
                var22 = Utils.limited(
                    0, (yvalues[0]!![var21].toDouble() + 100.5).toInt(), 199
                )
                var26[var22]!![1] = var26[var22]!![0] * var26[var22]!![1] + yvalues[0]!![var21]
                var27 = var26[var22]
                var27!![0]++
                var27 = var26[var22]
                var27!![1] /= var26[var22]!![0]
                ++var21
                var3 = var1
                var5 = var7
            }
            var25 = Utils.limited(1, (100.5 + (var3 + var5) * 0.5).toInt(), 198)
            var22 = 0
            var24 = 0
            var21 = 0
            while (var21 < var25 + 1) {
                var23 = var22
                if (var22.toFloat() < var26[var21]!![0]) {
                    var23 = var26[var21]!![0].toInt()
                    var24 = var21
                }
                ++var21
                var22 = var23
            }
            var1 = getGraphCodeToValue(0, var26[var24]!![1]).toDouble()
            var22 = var25 - 1
            var24 = 0
            var21 = var25 - 1
            while (var21 < 200) {
                var23 = var22
                if (var22.toFloat() < var26[var21]!![0]) {
                    var23 = var26[var21]!![0].toInt()
                    var24 = var21
                }
                ++var21
                var22 = var23
            }
            var7 = getGraphCodeToValue(0, var26[var24]!![1]).toDouble()
            runLater {
                view.amps1Txt.text = "a1(sine): " + getPrefixFormat(
                    (0.5 * (getGraphCodeToValue(0, var3.toFloat()) - getGraphCodeToValue(
                        0, var5.toFloat()
                    )).toDouble()).toFloat(), 4
                ) + "V"
                view.ampp1Txt.text = "a1(puls): " + getPrefixFormat((var7 - var1).toFloat(), 4) + "V"
            }
            var1 = 0.0
            var11 = (var3 + var5) * 0.5
            var22 = yvalues[0]!!.size
            if (var3 - var5 < 10.0) {
                var1 = var22.toDouble()
            } else {
                var19 = var11 - 0.5 * (var11 - var5)
                var7 = nextIntRise(yvalues[0], 0, var22, var19)
                var9 = nextIntRise(yvalues[0], var7.toInt(), var22, var11)
                var7 = var9
                var21 = 0
                while (var7 < (var22 - 1).toDouble()) {
                    var7 = nextIntRise(yvalues[0], var7.toInt(), var22, var11 + 0.5 * (var3 - var11))
                    if (var7 > (var22 - 1).toDouble()) {
                        break
                    }
                    var7 = nextIntFall(yvalues[0], var7.toInt(), var22, var19)
                    if (var7 > (var22 - 1).toDouble()) {
                        break
                    }
                    var7 = nextIntRise(yvalues[0], var7.toInt(), var22, var11)
                    if (var7 > (var22 - 1).toDouble()) {
                        break
                    }
                    var1 = var7
                    ++var21
                }
                var1 = if (var21 != 0) {
                    (var1 - var9) / var21.toDouble()
                } else {
                    var22.toDouble()
                }
            }
            var9 = var1
            runLater {
                view.freq1Txt.text = "f1: " + getPrefixFormat(
                    (1.0 / getGraphTimeToValue(var1.toFloat()).toDouble()).toFloat(), 4
                ) + "Hz"
            }
            var11 = var5
            var7 = var3
        } else {
            runLater {
                view.freq1Txt.text = "f1: --- Hz"
                view.amps1Txt.text = "a1(sine): --- V"
                view.ampp1Txt.text = "a1(puls): --- V"
            }
        }
        var21 = 0
        while (var21 < 200) {
            var26[var21]!![0] = 0f
            var26[var21]!![1] = 0f
            ++var21
        }
        if (ch2Enabled && yvalues[1]!!.isNotEmpty()) {
            var5 = yvalues[1]!![0].toDouble()
            var3 = var5
            var21 = 0
            while (var21 < yvalues[1]!!.size) {
                var1 = var3
                if (var3 < yvalues[1]!![var21].toDouble()) {
                    var1 = yvalues[1]!![var21].toDouble()
                }
                var13 = var5
                if (var5 > yvalues[1]!![var21].toDouble()) {
                    var13 = yvalues[1]!![var21].toDouble()
                }
                var22 = Utils.limited(
                    0, (yvalues[1]!![var21].toDouble() + 100.5).toInt(), 199
                )
                var26[var22]!![1] = var26[var22]!![0] * var26[var22]!![1] + yvalues[1]!![var21]
                var27 = var26[var22]
                var27!![0]++
                var27 = var26[var22]
                var27!![1] /= var26[var22]!![0]
                ++var21
                var3 = var1
                var5 = var13
            }
            var25 = Utils.limited(1, (100.5 + (var3 + var5) * 0.5).toInt(), 198)
            var22 = 0
            var24 = 0
            var21 = 0
            while (var21 < var25 + 1) {
                var23 = var22
                if (var22.toFloat() < var26[var21]!![0]) {
                    var23 = var26[var21]!![0].toInt()
                    var24 = var21
                }
                ++var21
                var22 = var23
            }
            var1 = getGraphCodeToValue(1, var26[var24]!![1]).toDouble()
            var22 = var25 - 1
            var24 = 0
            var21 = var25 - 1
            while (var21 < 200) {
                var23 = var22
                if (var22.toFloat() < var26[var21]!![0]) {
                    var23 = var26[var21]!![0].toInt()
                    var24 = var21
                }
                ++var21
                var22 = var23
            }
            var13 = getGraphCodeToValue(1, var26[var24]!![1]).toDouble()
            runLater {
                view.amps2Txt.text = "a2(sine): " + getPrefixFormat(
                    (0.5 * (getGraphCodeToValue(1, var3.toFloat()) - getGraphCodeToValue(
                        1, var5.toFloat()
                    )).toDouble()).toFloat(), 4
                ) + "V"
                view.ampp2Txt.text = "a2(puls): " + getPrefixFormat((var13 - var1).toFloat(), 4) + "V"
            }
            var1 = 0.0
            var17 = (var3 + var5) * 0.5
            var22 = yvalues[1]!!.size
            if (var3 - var5 < 10.0) {
                var1 = var22.toDouble()
            } else {
                var19 = var17 - 0.5 * (var17 - var5)
                var13 = nextIntRise(yvalues[1], 0, var22, var19)
                var15 = nextIntRise(yvalues[1], var13.toInt(), var22, var17)
                var13 = var15
                var21 = 0
                while (var13 < (var22 - 1).toDouble()) {
                    var13 = nextIntRise(yvalues[1], var13.toInt(), var22, var17 + 0.5 * (var3 - var17))
                    if (var13 > (var22 - 1).toDouble()) {
                        break
                    }
                    var13 = nextIntFall(yvalues[1], var13.toInt(), var22, var19)
                    if (var13 > (var22 - 1).toDouble()) {
                        break
                    }
                    var13 = nextIntRise(yvalues[1], var13.toInt(), var22, var17)
                    if (var13 > (var22 - 1).toDouble()) {
                        break
                    }
                    var1 = var13
                    ++var21
                }
                var1 = if (var21 != 0) {
                    (var1 - var15) / var21.toDouble()
                } else {
                    var22.toDouble()
                }
            }
            var13 = var1
            runLater {
                view.freq2Txt.text = "f2: " + getPrefixFormat(
                    (1.0 / getGraphTimeToValue(var1.toFloat()).toDouble()).toFloat(), 4
                ) + "Hz"
            }
        } else {
            runLater {
                view.freq2Txt.text = "f2: --- Hz"
                view.amps2Txt.text = "a2(sine): --- V"
                view.ampp2Txt.text = "a2(puls): --- V"
            }
            var3 = var17
            var5 = var15
        }
        if (autoControl > 0) {
            doAutoControl(var11.toInt(), var7.toInt(), var9.toInt(), var5.toInt(), var3.toInt(), var13.toInt())
        }
        println("AOPA" + "doMeas exit")
    }

    fun onDestroy() {
        pACKScopeDrv?.pAULNetConnection?.closeDevice()
        startMTimer(false)
        pACKScopeDrv?.closeEndPoints()
    }

    fun handleBtnClose() {
        onDestroy()
        Platform.exit()
    }

    fun handleBtnSave() {
        println("handleBtnSave()")
    }

    fun handleBtnOur() {
        pACKScopeDrv?.pAULNetConnection?.isDeviceExists()
    }

    fun handleBtnLoad() {
        println("handleBtnLoad() getRFileName()")
    }

    fun handleBtnAutoSet() {
        autoControl(-1)
    }

    fun handleBtnHelp() {
        println("handelBtnHelp")
    }

    fun handleCtrlShowBtn() {
        val isNeedShow: Boolean = view.ctrlLayout.isVisible
        view.ctrlLayout.isVisible = isNeedShow
        view.trglevelLayout.isVisible = isNeedShow
        view.ofs1Layout.isVisible = isNeedShow
        view.ofs2Layout.isVisible = isNeedShow
    }

    fun handleRunBtn() {
        startMTimer(view.runBtn.isSelected)
    }

    override fun onANConnect(aulNetConnection: AULNetConnection?) {
        val longName = "R.string.app_longname"
        val txtDevName = aulNetConnection!!.devname
        val headerName = """
            $longName
            $txtDevName
            """.trimIndent()
        if (pACKScopeDrv != null) {
            if (pACKScopeDrv?.readMemorySize() ?: 0 < 100000) {
                val list = FXCollections.observableArrayList<String>()
                list.add("1K p")
                list.add("10K p")
                list.add("50K p")
                view.dataLenSpinner.items = list
            }
//            if (loadData("/AOPA/default.csv", true) != 1) {
            setDefaultScopeSettings()
            autoControl(15)
//            }
            dataWaiting = 0
        startMTimer(true)
        }
        println("AOPA" + "onANConnect exit")
    }

    override fun onDataReady(ackScopeDrv: ACKScopeDrv?) {
        if (view.runBtn.isSelected) {
            var dataLength = pACKScopeDrv?.data1?.size ?: 0
            if (dataLength > 0) {
                noVoidData = true
            }
            xvalues = FloatArray(dataLength)
            yvalues[0] = FloatArray(dataLength)
            dataLength = 0
            while (dataLength < xvalues.size) {
                xvalues[dataLength] = ((dataLength + 1).toDouble() * 204.8 / xvalues.size.toDouble() - 102.4).toFloat()
                val hz = (pACKScopeDrv?.data1?.get(dataLength) ?: 0).toInt() and 255
                yvalues[0]!![dataLength] = channelCodeToValue(0, hz)
                ++dataLength
            }
            dataLength = pACKScopeDrv?.data2?.size ?: 0
            if (dataLength > 0) {
                noVoidData = true
            }
            xvalues = FloatArray(dataLength)
            yvalues[1] = FloatArray(dataLength)
            dataLength = 0
            while (dataLength < xvalues.size) {
                xvalues[dataLength] = ((dataLength + 1).toDouble() * 204.8 / xvalues.size.toDouble() - 102.4).toFloat()
                val hz2 = (pACKScopeDrv?.data2?.get(dataLength) ?: 0).toInt() and 255
                yvalues[1]!![dataLength] = channelCodeToValue(1, hz2)
                ++dataLength
            }
        }
        dataWaiting = 2
    }

    override fun onRegStatusChange(ackScopeDrv: ACKScopeDrv?) {}
}
