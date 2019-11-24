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


private class BciStreamProcessor(
    private val appConfig: AppConfig, private val bciPlotView: BCIPlotView
) {
    companion object {
        val LOG: Logger = LogManager.getLogger(BciStreamProcessor::class.qualifiedName)
    }

    private var cyton: ICyton? = null

    fun init() {
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

    fun start() {
        LOG.info("Starting BCI stream")
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

    fun stop() {
        LOG.info("Stopping BCI stream")
        bciPlotView.stopCharts()
        cyton?.stopStreaming()
        cyton?.close()
        cyton = null
    }
}

class RotationStreamReceiver(private val rotationView: RotationView) : RotationStreamProcessor() {
    override fun onNextCallback(data: RotationStreamProcessorService.RotationData) {
        rotationView.chart.add(data.timestamp, data.velocity)
        rotationView.image.rotate += data.velocity
        rotationView.statusBar.active()
    }

    override fun onErrorCallback(t: Throwable) {
        rotationView.statusBar.error()
    }

    override fun onCompleteCallback() {
        rotationView.statusBar.completed()
    }

    override fun start(host: String, port: Int) {
        super.start(host, port)
        rotationView.chart.start()
        rotationView.statusBar.started()
    }

    override fun stop() {
        super.stop()
        rotationView.chart.stop()
        rotationView.statusBar.inactive()
    }
}

class AppView : View("Rotation Task") {
    companion object {
        val LOG: Logger = LogManager.getLogger(AppView::class.qualifiedName)
    }

    override val configPath: Path = app.configBasePath.resolve("RotationTaskConfig.properties")
    private val appConfig = AppConfig(this.config)

    private val appView = RotationView()
    override val root = appView.root

    private var bciPlotView = find<BCIPlotView>(appConfig)
    private var bciPlotWindow = bciPlotView.openWindow(
        modality = Modality.NONE, owner = this.currentWindow, escapeClosesWindow = false
    )

    private val rotationStreamReceiver = RotationStreamReceiver(appView)
    private val bciStreamProcessor = BciStreamProcessor(appConfig, bciPlotView)

    init {
        appView.menuBar.startMenu.apply {
            selectedProperty().addListener {_, _, selected ->
                if (selected) {
                    text = "Stop Streaming"
                    action {
                        rotationStreamReceiver.start(appConfig.grpcHost, appConfig.grpcPort)
                        bciStreamProcessor.init()
                        bciStreamProcessor.start()
                    }
                } else {
                    text = "Start Streaming"
                    action {
                        rotationStreamReceiver.stop()
                        bciStreamProcessor.stop()
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
        rotationStreamReceiver.stop()
        bciStreamProcessor.stop()
        bciPlotView.close()
        find<MainView>().openWindow()
    }
}
