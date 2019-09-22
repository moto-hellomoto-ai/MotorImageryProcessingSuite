package ai.hellomoto.mip.common_fragments

import javafx.scene.chart.XYChart
import tornadofx.*

class ChartFragment(val hideXAxis:Boolean=true) : Fragment() {
    override val root: CustomLineChart<String, Float> by fxml()
    val chart: CustomLineChart<String, Float> by fxid("chart")
    val series: XYChart.Series<String, Float> = chart.series("Plot")

    init {
        if (hideXAxis) { chart.hideXAxis() }
        chart.style = "-fx-border-color: black"
    }
}
