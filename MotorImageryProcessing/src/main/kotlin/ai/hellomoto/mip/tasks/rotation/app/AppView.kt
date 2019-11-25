package ai.hellomoto.mip.tasks.rotation.app

import ai.hellomoto.mip.MainView
import ai.hellomoto.mip.openbci.OperationResult
import ai.hellomoto.mip.tasks.rotation.app.processors.BCIController
import ai.hellomoto.mip.tasks.rotation.app.processors.DataProcessor
import ai.hellomoto.mip.tasks.rotation.app.processors.RotationStreamProcessor
import javafx.application.Platform
import javafx.stage.Modality
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import tornadofx.*
import java.nio.file.Path


class AppView : View("Rotation Task") {
    companion object {
        val LOG: Logger = LogManager.getLogger(AppView::class.qualifiedName)
    }

    override val configPath: Path = app.configBasePath.resolve("RotationTaskConfig.properties")
    override val scope = AppConfig(this.config)

    val rotationView: RotationView by inject()
    override val root = rotationView.root

    private val appConfigView = find<AppConfigView>()

    val bciPlotView = find<BCIPlotView>()
    private val bciPlotWindow = bciPlotView.openWindow(
        modality = Modality.NONE, owner = this.currentWindow, escapeClosesWindow = false
    )!!

    private val dataProcessor = DataProcessor(this)
    private val rotationStreamReceiver = RotationStreamProcessor(dataProcessor)
    private val bciStreamProcessor = BCIController(dataProcessor)

    init {
        appConfigView.apply acv@ {
            connectButton.apply {
                action {
                    runAsync {
                        if (this@acv.isStreaming.value) {
                            stopStreaming()
                        } else {
                            val success = startStreaming(this@acv.bciConfig, this@acv.networkConfig)
                            if (success) {
                                this@acv.commit()
                                scope.save()
                                Platform.runLater { this@acv.close() }
                            }
                        }
                    }
                }
            }
        }
        rotationView.apply {
            showBCIPlotMenu.apply {
                textProperty().bind(stringBinding(textProperty(), bciPlotWindow.showingProperty()) {
                    if (bciPlotWindow.isShowing) "Hide BCI Plot" else "Show BCI Plot"
                })
                action {
                    if (bciPlotWindow.isShowing) {
                        bciPlotWindow.close()
                    } else {
                        bciPlotWindow.show(); bciPlotWindow.requestFocus()
                    }
                }
            }
            streamMenu.apply {
                action { appConfigView.openModal() }
            }
            quitMenu.apply { action { close() } }
        }
    }

    private fun FXTask<*>.stopStreaming() {
        updateMessage("Stopping Open BCI...")
        rotationStreamReceiver.stop()
        updateMessage("Stopping GRPC Server ...")
        bciStreamProcessor.stop()
        updateMessage("Stopping Data Processor ...")
        dataProcessor.stopPlot()
        dataProcessor.stopFlush()
        updateMessage("")
        Platform.runLater{ appConfigView.isStreaming.set(false) }
    }

    private fun FXTask<*>.startStreaming(bciConfig: BCIConfig, grpcConfig: GrpcConfig): Boolean {
        updateMessage("Initializing Open BCI...")
        val result = bciStreamProcessor.init(bciConfig.serialPort, bciConfig.numChannels)
        if (result !is OperationResult.Success) {
            updateMessage("Failed to initialize Open BCI...")
            Platform.runLater{ appConfigView.isStreaming.set(false) }
            return false
        }
        Platform.runLater { bciPlotView.initCharts(bciConfig.numChannels) }
        updateMessage("Starting Open BCI Streaming...")
        bciStreamProcessor.start()
        updateMessage("Initializing GRPC Server...")
        rotationStreamReceiver.start(grpcConfig.host, grpcConfig.port)
        updateMessage("Initializing Data Processor")
        dataProcessor.startPlot()
        dataProcessor.startFlush()
        updateMessage("")
        Platform.runLater{ appConfigView.isStreaming.set(true) }
        return true
    }

    override fun onDock() {
        super.onDock()
        this.currentWindow?.let { scope.restoreWindowPosition("rotation_task", it) }
        bciPlotWindow.show()
    }

    override fun onUndock() {
        super.onUndock()
        this.currentWindow?.let { scope.storeWindowPosition("rotation_task", it) }
        runAsync {
            stopStreaming()
        }
        bciPlotView.close()
        find<MainView>().openWindow()
    }
}
