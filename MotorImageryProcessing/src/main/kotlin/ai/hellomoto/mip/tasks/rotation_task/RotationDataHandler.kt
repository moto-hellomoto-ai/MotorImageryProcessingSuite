package ai.hellomoto.mip.tasks.rotation_task

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import java.text.SimpleDateFormat
import java.util.*

class RotationDataHandler(private val series_data: ObservableList<XYChart.Data<String, Number>>) {
    private val dateFormat = SimpleDateFormat("HH:mm:ss:SSSS")
    private var lastUpdate: Long = 0

    fun handleMessage(data: RotationStreamProcessorService.RotationData) {
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