package ai.hellomoto.mip.tasks.rotation.app.fragments

import javafx.application.Platform
import javafx.geometry.Side
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.util.StringConverter
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DateLabelFormatter : StringConverter<Number>() {
    private val formatter = SimpleDateFormat("HH:mm:ss")
    override fun fromString(p0: String?): Number = 0
    override fun toString(p0: Number?): String = if (p0 == null) "" else formatter.format(Date(p0.toLong()))
}

class EmptyLabelFormatter : StringConverter<Number>() {
    override fun fromString(p0: String?): Number = 0
    override fun toString(p0: Number?): String = ""
}

class SimpleLineChart(hideXAxis: Boolean=true) : LineChart<Number, Number>(
    NumberAxis().apply {
        animated = false
        side = Side.BOTTOM
        prefHeight = if (hideXAxis) 0.0 else 13.0
        tickUnit = 1000.0
        tickLabelFormatter = if (hideXAxis) EmptyLabelFormatter() else DateLabelFormatter()
        isForceZeroInRange = false
        isAutoRanging = false
    },
    NumberAxis().apply {
        animated = false
        side = Side.LEFT
        isForceZeroInRange = false
    }
) {
    init {
        createSymbols = false
        animated = false
    }
}

class SimpleTimeSeries(hideAxis: Boolean=true, private val duration: Long=5000) : Fragment() {
    override val root = SimpleLineChart(hideAxis)

    private val data = root.series("Plot").data
    private val buffer: ArrayList<XYChart.Data<Number, Number>> = arrayListOf()
    private val bufferLock = Object()
    private var lastUpdate: Long = System.currentTimeMillis()

    fun add(x:Number, y: Number) {
        synchronized(bufferLock) {
            buffer.add(XYChart.Data(x, y))
        }
        val now = System.currentTimeMillis()
        if (now - lastUpdate > 30) {
            lastUpdate = now
            Platform.runLater { flushAndTruncate() }
        }
    }

    private fun flushAndTruncate() {
        synchronized(bufferLock) {
            // TODO: Add subsampling
            if (buffer.size > 0) {
                data.addAll(buffer)
            }
            buffer.clear()
        }
        val now = Date().time.toDouble()
        val then = now - duration
        var index = 0
        while (index < data.size && data[index].xValue.toDouble() < then) {
            index += 1
        }
        data.remove(0, index)
        val xAxis = (root.xAxis as NumberAxis)
        xAxis.lowerBound = then
        xAxis.upperBound = now
    }
}