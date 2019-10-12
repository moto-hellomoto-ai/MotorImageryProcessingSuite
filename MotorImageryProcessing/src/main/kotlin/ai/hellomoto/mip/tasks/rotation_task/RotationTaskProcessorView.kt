package ai.hellomoto.mip.tasks.rotation_task

import ai.hellomoto.mip.MainView
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.ToggleButton
import tornadofx.*


class RotationTaskProcessorView : View() {
    private var chart: LineChart<String, Number> by singleAssign()

    override val root = vbox {
        menubar {
            menu {
                text = "Rotation Task"
                item("Quit") {
                    action { replaceWith<MainView>() }
                }
            }
        }
        chart = linechart("Rotation",
            CategoryAxis().apply {
                animated = false
                side = Side.BOTTOM
                prefHeight = 0.0
            },
            NumberAxis().apply {
                animated = false
                side = Side.LEFT
                isForceZeroInRange = false
            }
        ) {
            createSymbols = false
            animated = false
            verticalGridLinesVisible = false
        }
        togglebutton("Start") {
            isSelected = false
            action { onToggle(this) }
        }
    }

    private val data: ObservableList<XYChart.Data<String, Number>> = chart.series("Plot").data

    private var bgServer: io.grpc.Server? = null
    private fun onToggle(button: ToggleButton) {
        if (button.isSelected) {
            data.clear()
            val handler = RotationDataHandler(data)
            bgServer = getServer(port = 59898) { data -> handler.handleMessage(data) }
            bgServer?.start()
            button.text = "Stop"
        } else {
            bgServer?.shutdownNow()
            bgServer = null
            button.text = "Start"
        }
    }
}