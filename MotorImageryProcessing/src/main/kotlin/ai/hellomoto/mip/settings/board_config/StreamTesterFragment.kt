package ai.hellomoto.mip.settings.board_config

import ai.hellomoto.mip.common_fragments.CustomLineCharts
import javafx.application.Platform
import javafx.scene.chart.XYChart
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


class StreamTesterFragment : Fragment() {
    companion object {
        val LOG: Logger = LogManager.getLogger(StreamTesterFragment::class.qualifiedName)
    }

    private val charts: CustomLineCharts = CustomLineCharts()

    override val root =  borderpane{
        center = charts
        bottom = flowpane {
            button("Start") {
                action {
                    if (isStreaming) {
                        stopStreaming()
                        this.text = "Start"
                    } else {
                        charts.initCharts(8)
                        startStreaming()
                        this.text = "Stop"
                    }
                }
            }
        }
    }

    private var isStreaming:Boolean = false
    private var streamingTimer: Timer? = null
    private var streamingTimerTask: TimerTask? = null
    private fun startStreaming() {
        val period:Long = 10
        val rand = Random()
        val dateFormat = SimpleDateFormat("HH:mm:ss:SSSS")
        streamingTimer = Timer(true)
        streamingTimerTask = streamingTimer?.scheduleAtFixedRate(0, period) {
            val t = dateFormat.format(Date())
            Platform.runLater {
                for (i in 0 until charts.dataList.size) {
                    addDataPoint(i, t, rand.nextFloat())
                }
            }
        }
        isStreaming = true
    }
    private fun stopStreaming() {
        if (isStreaming) {
            LOG.info("Stopping stream")
            streamingTimerTask?.cancel()
            streamingTimerTask = null
            streamingTimer?.cancel()
            streamingTimer = null
            isStreaming = false
        }
    }

    override fun onUndock() {
        super.onUndock()
        stopStreaming()
    }

    private fun addDataPoint(index:Int, t:String, y:Float) {
        val series = charts.dataList[index]
        series.add(XYChart.Data(t, y))
        if (series.size > 300) {
            series.removeAt(0)
        }
    }
}