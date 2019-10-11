package ai.hellomoto.mip.common_fragments

import javafx.scene.chart.XYChart
import tornadofx.*

class ChartFragment(val hideXAxis: Boolean = true) : Fragment() {
    override val root = CustomLineChart()

    val series: XYChart.Series<String, Number> = root.series("Plot")

    init {
        if (hideXAxis) {
            root.hideXAxis()
        }
        // Debug
        root.style = "-fx-border-color: black"
    }
}
