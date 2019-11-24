package ai.hellomoto.mip.tasks.rotation.app

import ai.hellomoto.mip.openbci.PacketData
import ai.hellomoto.mip.tasks.rotation.app.fragments.SimpleStatusBar
import ai.hellomoto.mip.tasks.rotation.app.fragments.SimpleTimeSeries
import javafx.scene.layout.VBox
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import tornadofx.*
import java.util.*

class BCIPlotView : View() {
    companion object {
        val LOG: Logger = LogManager.getLogger(BCIPlotView::class.qualifiedName)
        val COLORS = arrayOf(
            "#486f81", "#6a8694", "#8b9ea7", "#adb6bb",
            "#cfcfcf", "#cbacb0", "#c48a93", "#ba6776"
        )
    }

    override val scope = super.scope as AppConfig
    override val config = scope.config

    val statusBar = SimpleStatusBar()
    override val root = borderpane {
        center = vbox {
            prefHeight = 640.0
            prefWidth = 480.0
            minHeight = 0.0  // Prevent center from hiding bottom when window is small
        }
        bottom = statusBar.root
    }
    private val plotArea = root.center as VBox

    private val chartList: ArrayList<SimpleTimeSeries> = arrayListOf()

    fun initCharts(numCharts: Int) {
        stopCharts()
        chartList.clear()
        plotArea.clear()
        for (i in 1..numCharts) {
            val chart = SimpleTimeSeries(hideAxis = i != numCharts).apply {
                root.usePrefHeight = true
                root.prefHeightProperty().bind(plotArea.heightProperty().divide(numCharts))
                root.style = "CHART_COLOR_1: ${COLORS[(i -1) % 8]} ;"
            }
            chartList.add(chart)
            plotArea.add(chart.root)
        }
    }

    fun addData(data: PacketData) {
        LOG.debug(data.eegs.size)
        for (i in data.eegs.indices) {
            chartList[i].add(data.date.time, data.eegs[i])
        }
    }

    fun startCharts() = chartList.forEach{ it.start() }
    fun stopCharts() = chartList.forEach{ it.stop() }

    override fun onDock() {
        super.onDock()
        this.currentWindow?.let { scope.restoreWindowPosition("bci_controller", it) }
    }

    override fun onUndock() {
        super.onUndock()
        this.currentWindow?.let { scope.storeWindowPosition("bci_controller", it) }
    }
}