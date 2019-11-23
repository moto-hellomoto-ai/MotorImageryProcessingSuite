package ai.hellomoto.mip.tasks.rotation.app

import ai.hellomoto.mip.MainView
import ai.hellomoto.mip.openbci.Cyton
import ai.hellomoto.mip.openbci.ICyton
import ai.hellomoto.mip.openbci.OperationResult
import ai.hellomoto.mip.openbci.ReadPacketResult
import ai.hellomoto.mip.tasks.rotation.app.processors.RotationStreamProcessor
import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorService
import javafx.stage.Modality
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import tornadofx.*
import java.nio.file.Path
import java.util.*

class RotationTaskView : View("Rotation Task") {
    companion object {
        val LOG: Logger = LogManager.getLogger(RotationTaskView::class.qualifiedName)
    }

    override val configPath: Path = app.configBasePath.resolve("RotationTaskConfig.properties")
    private val appConfig = AppConfig(this.config)

    private val appView = AppView()
    override val root = appView.root

    private var bciPlotView = find<BCIPlotView>(appConfig)
    private var bciPlotWindow = bciPlotView.openWindow(
        modality = Modality.NONE, owner = this.currentWindow, escapeClosesWindow = false
    )

    private val rotationStreamReceiver = object : RotationStreamProcessor() {
        override fun onNextCallback(data: RotationStreamProcessorService.RotationData) {
            appView.rotationView.chart.add(Date().time, data.velocity)
            appView.rotationView.image.rotate += data.velocity
            appView.statusBar.active()
        }

        override fun onErrorCallback(t: Throwable) {
            appView.statusBar.error()
        }

        override fun onCompleteCallback() {
            appView.statusBar.completed()
        }
    }

    var cyton: ICyton? = null;

    init {
        appView.menuBar.startMenu.apply {
            selectedProperty().addListener {_, _, selected ->
                if (selected) {
                    text = "Stop Streaming"
                    action {
                        startRotationStreamReceiver()
                        initBCI()
                        startBCIStream()
                    }
                } else {
                    text = "Start Streaming"
                    action {
                        stopRotationStreamReceiver()
                        stopBCIStream()
                    }
                }
            }
        }
        appView.menuBar.showBCIPlotMenu.apply {
            text = "Hide BCI Plot"
            action { bciPlotWindow?.close() }
        }
        bciPlotWindow?.apply {
            showingProperty().addListener { _, _, isShowing ->
                if (isShowing) {
                    appView.menuBar.showBCIPlotMenu.text = "Hide BCI Plot"
                    appView.menuBar.showBCIPlotMenu.action { this.close() }
                } else {
                    appView.menuBar.showBCIPlotMenu.text = "Show BCI Plot"
                    appView.menuBar.showBCIPlotMenu.action { this.show(); this.requestFocus() }
                }
            }
        }
        appView.menuBar.configMenu.apply {
            action {
                val appConfigView = find<AppConfigView>(appConfig)
                appConfigView.resetFields()
                appConfigView.openModal()
            }
        }
        appView.menuBar.quitMenu.apply { action { close() } }
    }

    override fun onDock() {
        super.onDock()
        this.currentWindow?.let { appConfig.restoreWindowPosition("rotation_task", it) }
        bciPlotWindow?.show()
    }

    override fun onUndock() {
        super.onUndock()
        this.currentWindow?.let { appConfig.storeWindowPosition("rotation_task", it) }
        this.appView.menuBar.startMenu.isSelected = false
        stopRotationStreamReceiver()
        stopBCIStream()
        bciPlotView.close()
        find<MainView>().openWindow()
    }

    private fun startRotationStreamReceiver() {
        rotationStreamReceiver.start(appConfig.grpcHost, appConfig.grpcPort)
        appView.statusBar.started()
        appView.rotationView.chart.start()
    }

    private fun stopRotationStreamReceiver() {
        appView.rotationView.chart.stop()
        appView.statusBar.inactive()
        rotationStreamReceiver.stop()
    }

    private fun initBCI() {
        LOG.info("Initializing BCI from {}", appConfig.bciSerialPort)
        bciPlotView.initCharts(appConfig.bciNumChannels)
        cyton = (
                if (appConfig.bciSerialPort == AppConfig.CYTON_MOCK_PORT) CytonMock()
                else Cyton(appConfig.bciSerialPort)
                ).let {
            when (val result = it.initBoard()) {
                is OperationResult.Success -> {
                    if (appConfig.bciNumChannels == 16) {
                        it.attachDaisy()
                    }
                    it
                }
                else -> null
            }
        }
    }

    private fun startBCIStream() {
        bciPlotView.startCharts()
        cyton?.let {
            LOG.info("{}", cyton)
            it.startStreaming { result ->
                bciPlotView.statusBar.active()
                when (result) {
                    is ReadPacketResult.Success -> bciPlotView.addData(result.data)
                    is ReadPacketResult.Fail -> bciPlotView.statusBar.error()
                }
            }
            bciPlotView.statusBar.started()
        }
    }

    private fun stopBCIStream() {
        bciPlotView.stopCharts()
        cyton?.stopStreaming()
        cyton?.close()
        cyton = null
    }
}
