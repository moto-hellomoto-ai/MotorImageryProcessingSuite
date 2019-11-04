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
    private var bciPlot = find<BCIPlotView>(appConfig)
    private var bciPlotWindow = bciPlot.openWindow(
        modality = Modality.NONE, owner = this.currentWindow, escapeClosesWindow = false
    )

    init {
        appView.menuBar.startRotationStreamReceiverButton.apply {
            action {
                if (this.isSelected) {
                    startRotationStreamReceiver()
                    bciPlot.initCharts(appConfig.bciNumChannels)
                    initBCI()
                    startBCIStream()
                    this.text = "Stop"
                } else {
                    stopRotationStreamReceiver()
                    stopBCIStream()
                    this.text = "Start"
                }
            }
        }
        appView.menuBar.configRotationStreamReceiverButton.apply {
            action {
                val appConfigView = find<AppConfigView>(appConfig)
                appConfigView.resetFields()
                appConfigView.openModal()
            }
        }
        appView.menuBar.quitMenu.apply { action { close() } }
        appView.menuBar.showBCIControllerMenu.apply {
            action {
                if (bciPlotWindow?.isShowing == true) {
                    bciPlotWindow?.close()
                    this.text = "Show BCI Controller"
                } else {
                    bciPlotWindow?.show()
                    bciPlotWindow?.requestFocus()
                    this.text = "Hide BCI Controller"
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        bciPlotWindow?.show()
        this.currentWindow?.let { appConfig.restoreWindowPosition("rotation_task", it) }
    }

    private fun startRotationStreamReceiver() {
        rotationStreamReceiver.start(appConfig.grpcHost, appConfig.grpcPort)
        appView.statusBar.started()
    }

    private fun stopRotationStreamReceiver() {
        rotationStreamReceiver.stop()
        appView.statusBar.inactive()
    }

    private fun initBCI() {
        LOG.info("Initializing BCI from {}", appConfig.bciSerialPort)
        if (appConfig.bciSerialPort == AppConfig.CYTON_MOCK_PORT) {
            cyton = CytonMock()
            return
        }
        cyton = Cyton(appConfig.bciSerialPort).let {
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
        cyton?.let {
            LOG.info("{}", cyton)
            it.startStreaming { result ->
                bciPlot.statusBar.active()
                when (result) {
                    is ReadPacketResult.Success -> {
                        val now = Date().time
                        for (i in 0..7) {
                            bciPlot.chartList[i].add(now, result.data.eegs[i])
                        }
                    }
                    is ReadPacketResult.Fail -> bciPlot.statusBar.error()
                }
            }
            bciPlot.statusBar.started()
        }
    }

    private fun stopBCIStream() {
        cyton?.stopStreaming()
        cyton?.close()
        cyton = null
    }

    override fun onUndock() {
        super.onUndock()
        this.currentWindow?.let { appConfig.storeWindowPosition("rotation_task", it) }
        stopRotationStreamReceiver()
        bciPlot.close()
        find<MainView>().openWindow()
    }
}
