package ai.hellomoto.mip.settings.board_config

import ai.hellomoto.mip.openbci.Cyton
import ai.hellomoto.mip.openbci.OperationResult
import ai.hellomoto.mip.openbci.SampleRate
import com.fazecast.jSerialComm.SerialPort
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.text.Font
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jetbrains.anko.doAsync
import tornadofx.*

class BoardConfigFragment : Fragment("Board Configuration") {
    companion object {
        val LOG: Logger = LogManager.getLogger(BoardConfigFragment::class.qualifiedName)

        fun getPorts(): List<String> {
            return SerialPort.getCommPorts().map { it.systemPortName }
        }

        fun getSampleRates(): List<String> {
            return SampleRate.values().map { it.toString() }.asReversed()
        }
    }

    private var portSelector: ComboBox<String> by singleAssign()
    private var infoText: TextArea by singleAssign()
    private var configPane: AnchorPane by singleAssign()
    private var sampleRateSelector: ComboBox<String> by singleAssign()

    private val storedConfig = StoredConfig()

    override val root = vbox {
        prefHeight = 600.0
        prefWidth = 600.0
        toolbar {
            portSelector = combobox {
                promptText = "Serial Port"
                items = FXCollections.observableArrayList(getPorts())
                value = if (storedConfig.serialPort.value in items) {
                    storedConfig.serialPort.value
                } else {
                    items[0]
                }
            }
            button("Initialize") {
                action { withUIDisabled(this@BoardConfigFragment::initConfigPane) }
            }
            button("Test Stream") {
                action { withUIDisabled(this@BoardConfigFragment::testStream) }
            }
        }
        infoText = textarea {
            isEditable = false
            font = Font.font("Comic Sans MS", 13.0)
            prefHeight = 280.0
            maxHeight = 280.0
            vboxConstraints {
                marginTop = 8.0
                marginBottom = 4.0
                marginLeft = 8.0
                marginRight = 8.0
            }
        }
        configPane = anchorpane {
            isDisable = true
            sampleRateSelector = combobox {
                items = FXCollections.observableArrayList(getSampleRates())
                value = if (storedConfig.sampleRate.value in items) {
                    storedConfig.sampleRate.value
                } else {
                    items[0]
                }
            }
            prefHeight = 560.0
            vboxConstraints {
                marginTop = 4.0
                marginLeft = 8.0
            }
        }
    }

    private fun withUIDisabled(action: () -> Unit) {
        doAsync {
            root.isDisable = true
            try {
                action()
            } finally {
                root.isDisable = false
            }
        }
    }

    private fun initConfigPane() {
        LOG.info("Connecting ${portSelector.value}")
        Cyton(portSelector.value).use {
            val result = it.initBoard()
            infoText.text = result.message
            if (result is OperationResult.Success) {
                storeDefault("serialPort", portSelector.value)
                configPane.isDisable = false
                // Check sample rate and set in combobox
                if (it.sampleRate != null) {
                    val sampleRate = it.sampleRate.toString()
                    Platform.runLater {
                        sampleRateSelector.value = sampleRate
                    }
                    storeDefault("sampleRate", sampleRate)
                }
            }
        }
    }

    private fun testStream() {
        LOG.info("Testing Streaming ...")
        Platform.runLater {
            find<StreamTesterFragment>().openModal()
        }
    }

    private fun storeDefault(key: String, value: String) {
        with(storedConfig.config) {
            set(key to value)
            save()
        }
    }
}
