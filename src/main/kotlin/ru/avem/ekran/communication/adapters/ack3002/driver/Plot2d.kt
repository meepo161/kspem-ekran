package ru.avem.ekran.communication.adapters.ack3002.driver

import javafx.scene.canvas.GraphicsContext
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import tornadofx.c
import kotlin.math.abs
import kotlin.math.roundToInt

class Plot2d(
    _numOfChannel: Int,
    plotSize: Int,
    axesLblVisible: Int,
    private val gc: GraphicsContext,
    private val height: Int = 600,
    private val width: Int = 600
) {
    private var axesLblVisible = 1

    var axisColor = c(0, 0, 0)

    var backColor = c(255, 255, 255)
    private val frame: Boolean
    private var locxAxis = 0f
    private var locyAxis = 0f
    private val margin: Float
    private var maxx = 0f
    private var maxy = 0f
    private var minx = 0f
    private var miny = 0f
    private var plotCount = 0

    var plots: Array<Plot?>
    private var scaleFactorX = 1.0f
    private var scaleFactorY = 1.0f
    private var smaxx = 0f
    private var smaxy = 0f
    private var sminx = 0f
    private var sminy = 0f
    private var strokeWidth = 4.0f
//    private var textBounds = Rect()

    var vectorLength = 0
    private var vectorOffset: Int
//    private var wallpath = Path()
    private val xAxis: Axis

    var xmarks: Array<XMark?>
    private val yAxis: Axis
    private fun fromPixel(var1: Float, var2: Float, var3: Float, var4: Int, var5: Float): Float {
        return ((var3 - var2) * (var4.toFloat() - var5 * var1) / ((1.0f - 2.0f * var5) * var1) + var2).toDouble().toFloat()
    }

    private val axes: Unit
        get() {
            maxx = maxX
            maxy = maxY
            minx = minX
            miny = minY
            if (maxx == minx) {
                maxx = (maxx.toDouble() + 0.5).toFloat()
                minx = (minx.toDouble() - 0.5).toFloat()
            }
            if (maxy == miny) {
                maxy = (maxy.toDouble() + 0.5).toFloat()
                miny = (miny.toDouble() - 0.5).toFloat()
            }
            locyAxis = if (yAxis.auto) {
                if (minx >= 0.0f) {
                    minx
                } else if (minx < 0.0f && maxx >= 0.0f) {
                    0.0f
                } else {
                    maxx
                }
            } else {
                yAxis.zero
            }
            locxAxis = if (xAxis.auto) {
                if (miny >= 0.0f) {
                    miny
                } else if (miny < 0.0f && maxy >= 0.0f) {
                    0.0f
                } else {
                    maxy
                }
            } else {
                xAxis.zero
            }
        }

    private fun getMax(var1: FloatArray, var2: Int): Float {
        var var5 = var2
        if (var2 >= var1.size) {
            var5 = 0
        }
        var var3: Float
        var var4: Float
        var3 = var1[var5]
        while (var5 < var1.size) {
            var4 = var3
            if (var1[var5] > var3) {
                var4 = var1[var5]
            }
            ++var5
            var3 = var4
        }
        return var3
    }

    private val maxX: Float
        get() {
            var var5 = false
            var var1 = xAxis.max
            var var2 = var1
            if (xAxis.auto) {
                if (xAxis.autorange > 0.0f && xAxis.autozero) {
                    var2 = (xAxis.zero.toDouble() + 0.5 * xAxis.autorange.toDouble()).toFloat()
                } else {
                    var var4 = 0
                    while (var4 < plotCount) {
                        val var3 = getMax(plots[var4]!!.xvalues, vectorOffset)
                        var var6 = var5
                        var2 = var1
                        if (plots[var4]!!.visible && (!var5 || var1 < var3)) {
                            var2 = var3
                            var6 = true
                        }
                        ++var4
                        var5 = var6
                        var1 = var2
                    }
                }
            }
            return var2
        }

    private val maxY: Float
        get() {
            var var5 = false
            var var1 = yAxis.max
            var var2 = var1
            if (yAxis.auto) {
                if (yAxis.autorange > 0.0f && yAxis.autozero) {
                    var2 = (yAxis.zero.toDouble() + 0.5 * yAxis.autorange.toDouble()).toFloat()
                } else {
                    var var4 = 0
                    while (var4 < plotCount) {
                        val var3 = getMax(plots[var4]!!.yvalues, vectorOffset)
                        var var6 = var5
                        var2 = var1
                        if (plots[var4]!!.visible && (!var5 || var1 < var3)) {
                            var2 = var3
                            var6 = true
                        }
                        ++var4
                        var5 = var6
                        var1 = var2
                    }
                }
            }
            return var2
        }

    private fun getMin(var1: FloatArray, var2: Int): Float {
        var var5 = var2
        if (var2 >= var1.size) {
            var5 = 0
        }
        var var3: Float
        var var4: Float
        var3 = var1[var5]
        while (var5 < var1.size) {
            var4 = var3
            if (var1[var5] < var3) {
                var4 = var1[var5]
            }
            ++var5
            var3 = var4
        }
        return var3
    }

    private val minX: Float
        get() {
            var var5 = false
            var var1 = xAxis.min
            var var2 = var1
            if (xAxis.auto) {
                if (xAxis.autorange > 0.0f) {
                    var2 = maxX - xAxis.autorange
                } else {
                    var var4 = 0
                    while (var4 < plotCount) {
                        val var3 = getMin(plots[var4]!!.xvalues, vectorOffset)
                        var var6 = var5
                        var2 = var1
                        if (plots[var4]!!.visible && (!var5 || var1 > var3)) {
                            var2 = var3
                            var6 = true
                        }
                        ++var4
                        var5 = var6
                        var1 = var2
                    }
                }
            }
            return var2
        }

    private val minY: Float
        get() {
            var var5 = false
            var var1 = yAxis.min
            var var2 = var1
            if (yAxis.auto) {
                if (yAxis.autorange > 0.0f) {
                    var2 = maxY - yAxis.autorange
                } else {
                    var var4 = 0
                    while (var4 < plotCount) {
                        val var3 = getMin(plots[var4]!!.yvalues, vectorOffset)
                        var var6 = var5
                        var2 = var1
                        if (plots[var4]!!.visible && (!var5 || var1 > var3)) {
                            var2 = var3
                            var6 = true
                        }
                        ++var4
                        var5 = var6
                        var1 = var2
                    }
                }
            }
            return var2
        }

    init {
        var numOfChannel = _numOfChannel
        plots = arrayOfNulls(numOfChannel)
        this.axesLblVisible = axesLblVisible
        plotCount = plots.size
        if (plotCount > 0) {
            numOfChannel = 0
            while (numOfChannel < plotCount) {
                plots[numOfChannel] = Plot(plotSize)
                ++numOfChannel
            }
            vectorLength = plots[0]!!.xvalues.size
        } else {
            vectorLength = 0
        }
        vectorOffset = 0
        margin = 0.05f
        frame = true
        xAxis = Axis()
        yAxis = Axis()
        xmarks = arrayOfNulls(10)
        numOfChannel = 0
        while (numOfChannel < 10) {
            xmarks[numOfChannel] = XMark(numOfChannel)
            ++numOfChannel
        }
        axes
    }

    private fun toPixel(var1: Float, var2: Float, var3: Float, plotsValues: FloatArray, var5: Float): IntArray {
        val var7 = DoubleArray(plotsValues.size)
        val var8 = IntArray(plotsValues.size)
        for (i in plotsValues.indices) {
            var7[i] = (var5 * var1 + (plotsValues[i] - var2) / (var3 - var2) * (1.0f - 2.0f * var5) * var1).toDouble()
            var8[i] = var7[i].toInt()
        }
        return var8
    }

    private fun toPixelInt(total: Float, min: Float, max: Float, to: Float, margin: Float): Int {
        return (margin * total + (to - min) / (max - min) * (1.0f - 2.0f * margin) * total).toDouble().toInt()
    }

    fun scroll(_var1: Float, _var2: Float) {
        var var1 = _var1
        var var2 = _var2
        val height = height.toFloat()
        val width = width.toFloat()
        var1 = fromPixel(width, minx, maxx, var1.toInt(), margin) - fromPixel(width, minx, maxx, 0, margin)
        var2 = -(fromPixel(height, miny, maxy, var2.toInt(), margin) - fromPixel(height, miny, maxy, 0, margin))
        setXAxis(minx + var1, maxx + var1, xAxis.zero, xAxis.gridstep, false)
        setYAxis(miny + var2, maxy + var2, yAxis.zero, yAxis.gridstep, false)
    }

    fun zoom(_var1: Float, _var2: Float, _var3: Float, _var4: Float) {
        var var1 = _var1
        var var2 = _var2
        var var3 = _var3
        var var4 = _var4
        var var6 = height.toFloat()
        val var5 = fromPixel(width.toFloat(), minx, maxx, var3.toInt(), margin)
        var4 = fromPixel(var6, miny, maxy, var4.toInt(), margin)
        if (!java.lang.Double.isNaN(var1.toDouble()) && !java.lang.Double.isInfinite(var1.toDouble())) {
            var3 = var1
            if (abs(var1) > 10.0f) {
                var3 = 1.0f
            }
        }
        if (!java.lang.Double.isNaN(var2.toDouble()) && !java.lang.Double.isInfinite(var2.toDouble())) {
            var1 = var2
            if (abs(var2) > 10.0f) {
                var1 = 1.0f
            }
        }
        var2 = minx
        var6 = maxx
        if (var3 <= 0.0f) {
            setXAxis(sminx, smaxx, xAxis.zero, xAxis.gridstep, true)
        } else {
            setXAxis(var5 + (var2 - var5) * var3, var5 + (var6 - var5) * var3, xAxis.zero, xAxis.gridstep, false)
        }
        var2 = miny
        var3 = maxy
        if (var1 <= 0.0f) {
            setYAxis(sminy, smaxy, yAxis.zero, yAxis.gridstep, true)
        } else {
            setYAxis(var4 + (var2 - var4) * var1, var4 + (var3 - var4) * var1, yAxis.zero, yAxis.gridstep, false)
        }
    }

    @Synchronized fun onDraw() {
        val heightFloat = height.toFloat()
        val widthFloat = width.toFloat()
        val pixelIntLocXAxis = toPixelInt(heightFloat, miny, maxy, locxAxis, margin)
        val pixelIntLocYAxis = toPixelInt(widthFloat, minx, maxx, locyAxis, margin)
        val pixelIntMaxY = toPixelInt(heightFloat, miny, maxy, maxy, margin)
        val pixelIntMinY = toPixelInt(heightFloat, miny, maxy, miny, margin)
        val pixelIntMinX = toPixelInt(widthFloat, minx, maxx, minx, margin)
        val pixelIntMaxX = toPixelInt(widthFloat, minx, maxx, maxx, margin)
        gc.lineWidth = 0.5 * (1.0f + strokeWidth)
//        canvas.drawARGB(backColor ushr 24, backColor ushr 16 and 255, backColor ushr 8 and 255, backColor and 255)
        gc.fill = backColor
        gc.fillRect(0.0, 0.0, width.toDouble(), height.toDouble())
        gc.stroke = axisColor
        gc.strokeLine(pixelIntMinX.toDouble(), heightFloat - pixelIntLocXAxis.toDouble(), pixelIntMaxX.toDouble(), heightFloat - pixelIntLocXAxis.toDouble())
        gc.strokeLine(pixelIntLocYAxis.toDouble(), heightFloat - pixelIntMaxY.toDouble(), pixelIntLocYAxis.toDouble(), heightFloat - pixelIntMinY.toDouble())
        gc.fill = c(0, 0, 0, 0.5)
        gc.stroke = c(0, 0, 0, 0.5)
        if (yAxis.gridstep > 0.0f) {
            var deltaPlusX = locxAxis + yAxis.gridstep
            while (deltaPlusX < maxy) {
                val pixelIntDeltaPlusX = toPixelInt(heightFloat, miny, maxy, deltaPlusX, margin)
                gc.strokeLine(pixelIntMinX.toDouble(), heightFloat - pixelIntDeltaPlusX.toDouble(), pixelIntMaxX.toDouble(), heightFloat - pixelIntDeltaPlusX.toDouble())
                deltaPlusX += yAxis.gridstep
            }
            var deltaMinusX = locxAxis - yAxis.gridstep
            while (deltaMinusX > miny) {
                val pixelIntDeltaMinusX = toPixelInt(heightFloat, miny, maxy, deltaMinusX, margin)
                gc.strokeLine(pixelIntMinX.toDouble(), heightFloat - pixelIntDeltaMinusX.toDouble(), pixelIntMaxX.toDouble(), heightFloat - pixelIntDeltaMinusX.toDouble())
                deltaMinusX -= yAxis.gridstep
            }
        }
        if (xAxis.gridstep > 0.0f) {
            var deltaPlusY = locyAxis + xAxis.gridstep
            while (deltaPlusY < maxy) {
                val pixelIntDeltaPlusY = toPixelInt(widthFloat, minx, maxx, deltaPlusY, margin)
                gc.strokeLine(pixelIntDeltaPlusY.toDouble(), heightFloat - pixelIntMaxY.toDouble(), pixelIntDeltaPlusY.toDouble(), heightFloat - pixelIntMinY.toDouble())
                deltaPlusY += xAxis.gridstep
            }
            var deltaMinusY = locyAxis - xAxis.gridstep
            while (deltaMinusY > miny) {
                val pixelIntDeltaMinusY = toPixelInt(widthFloat, minx, maxx, deltaMinusY, margin)
                gc.strokeLine(pixelIntDeltaMinusY.toDouble(), heightFloat - pixelIntMaxY.toDouble(), pixelIntDeltaMinusY.toDouble(), heightFloat - pixelIntMinY.toDouble())
                deltaMinusY -= xAxis.gridstep
            }
        }
        if (frame) {
            gc.strokeLine(pixelIntMinX.toDouble(), heightFloat - pixelIntMaxY.toDouble(), pixelIntMaxX.toDouble(), heightFloat - pixelIntMaxY.toDouble())
            gc.strokeLine(pixelIntMinX.toDouble(), heightFloat - pixelIntMinY.toDouble(), pixelIntMaxX.toDouble(), heightFloat - pixelIntMinY.toDouble())
            gc.strokeLine(pixelIntMinX.toDouble(), heightFloat - pixelIntMaxY.toDouble(), pixelIntMinX.toDouble(), heightFloat - pixelIntMinY.toDouble())
            gc.strokeLine(pixelIntMaxX.toDouble(), heightFloat - pixelIntMaxY.toDouble(), pixelIntMaxX.toDouble(), heightFloat - pixelIntMinY.toDouble())
        }
//        gc.pathEffect = null
        gc.fill = axisColor
        if (axesLblVisible != 0) {
            gc.textAlign = TextAlignment.CENTER
            gc.font = Font.font(strokeWidth * 10.0)
            var i1 = 1
            while (i1 <= 4) {
                val minXValue = ((10.0f * (minx + (i1 - 1).toFloat() * (maxx - minx) / 4.toFloat())).roundToInt() / 10).toFloat()
                gc.fillText(minXValue.toString(), toPixelInt(widthFloat, minx, maxx, minXValue, margin).toDouble(), heightFloat - pixelIntLocXAxis.toFloat() + 20.0)
                val minYValue = ((10.0f * (miny + (i1 - 1).toFloat() * (maxy - miny) / 4.toFloat())).roundToInt() / 10).toFloat()
                gc.fillText(minYValue.toString(), (pixelIntLocYAxis + 20).toDouble(), heightFloat - toPixelInt(heightFloat, miny, maxy, minYValue, margin).toDouble())
                ++i1
            }
            gc.fillText(maxx.toString(), toPixelInt(widthFloat, minx, maxx, maxx, margin).toDouble(), heightFloat - pixelIntLocXAxis.toFloat() + 20.0)
            gc.fillText(maxy.toString(), (pixelIntLocYAxis + 20).toDouble(), heightFloat - toPixelInt(heightFloat, miny, maxy, maxy, margin).toDouble())
        }
        gc.lineWidth = strokeWidth.toDouble()
        var i2 = 0
        while (i2 < plotCount) {
            if (plots[i2]!!.visible) {
                val pixelsX = toPixel(widthFloat, minx, maxx, plots[i2]!!.xvalues, margin)
                val pixelsY = toPixel(heightFloat, miny, maxy, plots[i2]!!.yvalues, margin)
                var j = vectorOffset
                while (j < vectorLength - 1) {
                    gc.fill = plots[i2]!!.color
                    gc.stroke = plots[i2]!!.color
                    gc.strokeLine(
                        pixelsX[j].toDouble(),
                        heightFloat - pixelsY[j].toDouble(),
                        pixelsX[j + 1].toDouble(),
                        heightFloat - pixelsY[j + 1].toDouble()
                    )
                    ++j
                }
            }
            ++i2
        }
//        var i3 = 0
//        while (i3 < 10) {
//            if (xmarks[i3]!!.visible) {
//                gc.fill = xmarks[i3]!!.color
//                val pixelIntXmarksX = toPixelInt(widthFloat, minx, maxx, xmarks[i3]!!.x, margin)
//                gc.strokeLine(pixelIntXmarksX.toDouble(), heightFloat - pixelIntMaxY.toDouble(), pixelIntXmarksX.toDouble(), heightFloat - pixelIntMinY.toDouble())
//                if (xmarks[i3]!!.label.isNotEmpty()) {
////                    gc.pathEffect = null
//                    gc.textAlign = TextAlignment.CENTER
//                    gc.font = Font.font(18.0)
//                    gc.getTextBounds(xmarks[i3]!!.label, 0, xmarks[i3]!!.label.length, textBounds)
//                    val textBoundWidth = textBounds.width()
//                    val textBoundHeight = textBounds.height()
//                    val thirdOfBoundHeight = textBoundHeight / 3
//                    wallpath.reset()
//                    wallpath.moveTo((pixelIntXmarksX - thirdOfBoundHeight - textBoundWidth / 2).toFloat(), heightFloat - pixelIntMaxY.toFloat())
//                    wallpath.lineTo((pixelIntXmarksX + thirdOfBoundHeight + textBoundWidth / 2).toFloat(), heightFloat - pixelIntMaxY.toFloat())
//                    wallpath.lineTo(
//                        (pixelIntXmarksX + thirdOfBoundHeight + textBoundWidth / 2).toFloat(), heightFloat - pixelIntMaxY.toFloat() + textBoundHeight.toFloat() + thirdOfBoundHeight.toFloat()
//                    )
//                    wallpath.lineTo(pixelIntXmarksX.toFloat(), heightFloat - pixelIntMaxY.toFloat() + (textBoundHeight * 2).toFloat() + thirdOfBoundHeight.toFloat())
//                    wallpath.lineTo(
//                        (pixelIntXmarksX - thirdOfBoundHeight - textBoundWidth / 2).toFloat(), heightFloat - pixelIntMaxY.toFloat() + textBoundHeight.toFloat() + thirdOfBoundHeight.toFloat()
//                    )
//                    wallpath.lineTo((pixelIntXmarksX - thirdOfBoundHeight - textBoundWidth / 2).toFloat(), heightFloat - pixelIntMaxY.toFloat())
//                    canvas.drawPath(wallpath, gc)
//                    gc.fill = backColor  // -16777216 or backColor
//                    gc.fillText(
//                        xmarks[i3]!!.label, pixelIntXmarksX.toDouble(), heightFloat - pixelIntMaxY.toFloat() + textBoundHeight.toFloat() + thirdOfBoundHeight.toDouble()
//                    )
//                }
//            }
//            ++i3
//        }
    }

    fun setData(channel: Int, xValues: FloatArray, yValues: FloatArray, vectorOffset: Int, vectorLength: Int) {
        if (channel in 0 until plotCount) {
            plots[channel]!!.xvalues = xValues
            plots[channel]!!.yvalues = yValues
            this.vectorOffset = vectorOffset
            this.vectorLength = vectorLength
            axes
//            invalidate()
        }
    }

    private fun invalidate() {
//        onDraw()
    }

    fun setStrokeWidth(var1: Float) {
        strokeWidth = var1
    }

    fun setXAutorange(autorange: Float) {
        xAxis.autorange = autorange
        xAxis.autozero = false
    }

    fun setXAutorange(autoRange: Float, autoZero: Boolean) {
        xAxis.autorange = autoRange
        xAxis.autozero = autoZero
    }

    fun setXAxis(min: Float, max: Float, zero: Float, gridstep: Float, auto: Boolean) {
        xAxis.min = min
        xAxis.max = max
        xAxis.zero = zero
        xAxis.gridstep = gridstep
        xAxis.auto = auto
        axes
    }

    fun setXOffset(var1: Int) {
        vectorOffset = var1
    }

    fun setYAutorange(autorange: Float) {
        yAxis.autorange = autorange
        yAxis.autozero = false
    }

    fun setYAutorange(autorange: Float, autozero: Boolean) {
        yAxis.autorange = autorange
        yAxis.autozero = autozero
    }

    fun setYAxis(min: Float, max: Float, zero: Float, gridzero: Float, auto: Boolean) {
        yAxis.min = min
        yAxis.max = max
        yAxis.zero = zero
        yAxis.gridstep = gridzero
        yAxis.auto = auto
        axes
    }

    inner class Axis {
        var auto = true
        var autorange = 0.0f
        var autozero = false
        var gridstep = 20.0f
        var max = 100.0f
        var min = -100.0f
        var zero = 0.0f
    }

    inner class Plot(size: Int) {
        var color = c(255, 0, 0)
        var visible = false
        var xvalues = FloatArray(size)
        var yvalues = FloatArray(size)
    }

    inner class XMark(name: Int) {
        var color = c(0, 128, 0)
        var label = name.toString()
        var visible = false
        var x = 0.0f
    }
}
