package ai.hellomoto.mip.tasks.rotation.app

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

    val chartList: ArrayList<SimpleTimeSeries> = arrayListOf()

    fun initCharts(numCharts: Int) {
        plotArea.clear()
        chartList.clear()
        for (i in 1..numCharts) {
            val chart = SimpleTimeSeries(hideAxis = i != numCharts).apply {
                root.usePrefHeight = true
                root.prefHeightProperty().bind(plotArea.heightProperty().divide(numCharts))
            }
            plotArea.add(chart.root)
            chartList.add(chart)
        }
    }

    override fun onDock() {
        super.onDock()
        this.currentWindow?.let { scope.restoreWindowPosition("bci_controller", it) }
    }

    override fun onUndock() {
        super.onUndock()
        this.currentWindow?.let { scope.storeWindowPosition("bci_controller", it) }
    }
}