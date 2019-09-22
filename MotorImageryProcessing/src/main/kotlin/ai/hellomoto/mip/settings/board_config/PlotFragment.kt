package ai.hellomoto.mip.settings.board_config

import ai.hellomoto.mip.common_fragments.ChartFragment
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import tornadofx.Fragment
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.math.min


class PlotFragment : Fragment() {
    override val root: AnchorPane by fxml()
    private val charts: VBox by fxid("charts")
    private val serieses: ArrayList<ObservableList<XYChart.Data<String, Float>>> = arrayListOf()

    init {
        initChartContainer(4)
        initCharts(4)
        startStreaming()
    }

    private fun initChartContainer(numCharts: Int) {
        charts.prefWidthProperty().bind(root.widthProperty())
        charts.prefHeightProperty().bind(Bindings.createDoubleBinding(Callable<Double> {
            min(root.heightProperty().get() - 39.0, 100.0 * numCharts.toDouble())
        }, root.heightProperty()))
        charts.style = "-fx-border-color: blue"
    }

    private fun initCharts(numCharts:Int) {
        val chartHeightBinding = Bindings.createDoubleBinding(Callable<Double> {
            min(100.0, (root.heightProperty().get() - 39.0) / numCharts.toDouble())
        }, root.heightProperty())

        for (i in 1..numCharts) {
            val chartFragment = ChartFragment(hideXAxis = true)
            chartFragment.chart.prefHeightProperty().bind(chartHeightBinding)
            charts.add(chartFragment)
            serieses.add(chartFragment.series.data)
        }
    }

    private var streamingTimer: Timer? = null
    private var streamingTimerTask: TimerTask? = null
    fun startStreaming() {
        val period:Long = 10
        val rand = Random()
        val dateFormat = SimpleDateFormat("HH:mm:ss:SSSS")
        streamingTimer = Timer(true)
        streamingTimerTask = streamingTimer?.scheduleAtFixedRate(0, period) {
            val t = dateFormat.format(Date())
            Platform.runLater {
                for (i in 0 until serieses.size) {
                    addDataPoint(i, t, rand.nextFloat())
                }
            }
        }
    }
    fun stopStreaming() {
        streamingTimerTask?.cancel()
        streamingTimerTask = null
        streamingTimer?.cancel()
        streamingTimer = null
    }
    override fun onUndock() {
        super.onUndock()
        stopStreaming()
    }

    private fun addDataPoint(index:Int, t:String, y:Float) {
        val series = serieses[index]
        series.add(XYChart.Data(t, y))
        if (series.size > 300) {
            series.removeAt(0)
        }
    }
}
