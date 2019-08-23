package ai.hellomoto.mip.tasks.rotation_task

import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorService.RotationData
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import javafx.scene.control.ToggleButton
import javafx.scene.layout.AnchorPane
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.*


class Handler(private val series_data: ObservableList<XYChart.Data<String, Float>>) {
    private val dateFormat = SimpleDateFormat("HH:mm:ss:SSSS")
    private var lastUpdate:Long = 0

    fun handleMessage(data:RotationData) {
        val t = dateFormat.format(Date())
        val now = System.currentTimeMillis()
        if (now - lastUpdate > 60) {
            Platform.runLater {
                series_data.add(XYChart.Data(t, data.velocity))
                if (series_data.size > 300) {
                    series_data.removeAt(0)
                }
            }
            lastUpdate = now
        }
    }
}

class RotationTaskProcessorView: View() {

    override val root: AnchorPane by fxml()
    private val port: Int = 59898

    private val chart: LineChart<String, Float> by fxid("chart")
    private val series: XYChart.Series<String, Float>
    private var data: ObservableList<XYChart.Data<String, Float>>

    private val toggleButton: ToggleButton by fxid("toggleButton")
    private var bgServer: io.grpc.Server? = null

    init {
        series = chart.series("Plot") {}
        data = series.data

        toggleButton.action {this.onToggle()}
    }

    private fun onToggle()  {
        if (toggleButton.isSelected) {
            data.clear()
            val handler = Handler(data)
            bgServer = getServer(port=port) {data -> handler.handleMessage(data)}
            bgServer?.start()
            toggleButton.text = "ON"
        } else {
            bgServer?.shutdownNow()
            bgServer = null
            toggleButton.text = "OFF"
        }
    }
}
