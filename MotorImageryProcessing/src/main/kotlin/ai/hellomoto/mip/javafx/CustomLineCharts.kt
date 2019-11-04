package ai.hellomoto.mip.javafx

import javafx.beans.binding.Bindings
import javafx.beans.binding.DoubleBinding
import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import javafx.scene.layout.VBox
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import tornadofx.*
import java.util.*
import java.util.concurrent.Callable
import kotlin.math.min


class CustomLineCharts : VBox() {
    companion object {
        val LOG: Logger = LogManager.getLogger(CustomLineCharts::class.qualifiedName)
        const val MAX_CHART_HEIGHT = 80.0
    }

    val dataList: ArrayList<ObservableList<XYChart.Data<String, Number>>> = arrayListOf()

    init {
        prefHeight = 640.0
        style = "-fx-border-color: blue"
    }

    fun initCharts(numCharts:Int) {
        this.clear()
        dataList.clear()

        val heightBinding = getHeightBinding(numCharts)
        for (i in 1..numCharts) {
            val chart = CustomLineChart().hideXAxis()
            chart.usePrefHeight = true
            chart.prefHeightProperty().bind(heightBinding)
            this.add(chart)

            val data = chart.series("Plot").data;
            dataList.add(data)
        }
    }

    private fun getHeightBinding(numCharts:Int, maxHeight:Double=MAX_CHART_HEIGHT): DoubleBinding {
        val hProp = this.heightProperty()
        return Bindings.createDoubleBinding(Callable<Double> {
            min(maxHeight, hProp.get() / numCharts)
        }, hProp)
    }
}