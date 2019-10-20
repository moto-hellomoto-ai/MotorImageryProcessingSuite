package ai.hellomoto.mip.common_fragments

import javafx.geometry.Side
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis


class CustomLineChart : LineChart<String, Number>(
    CategoryAxis().apply {
        animated = false
        side = Side.BOTTOM
        prefHeight = 0.0
        isTickMarkVisible = false
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
        verticalGridLinesVisible = false
        // style = "-fx-border-color: red"
    }

    fun hideXAxis(): CustomLineChart {
        chartChildren.remove(this.xAxis)
        return this
    }
}