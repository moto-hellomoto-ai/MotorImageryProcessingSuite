package ai.hellomoto.mip.tasks.rotation.app

import ai.hellomoto.mip.openbci.SampleRate
import com.fazecast.jSerialComm.SerialPort
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.scene.control.Button
import javafx.scene.control.TextFormatter
import javafx.util.converter.IntegerStringConverter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import tornadofx.*
import java.io.File


private class BCIConfigModel(bciConfig: BCIConfig) : ItemViewModel<BCIConfig>(bciConfig) {
    val serialPort = bind(BCIConfig::serialPortProperty)
    val sampleRate = bind(BCIConfig::sampleRateProperty)
    val numChannels = bind(BCIConfig::numChannelsProperty)
}

private class IOConfigModel(ioConfig: IOConfig) : ItemViewModel<IOConfig>(ioConfig) {
    val saveDir = bind(IOConfig::saveDirProperty)
}

private class GrpcConfigModel(grpcConfig: GrpcConfig) : ItemViewModel<GrpcConfig>(grpcConfig) {
    val port = bind(GrpcConfig::portProperty)
    val host = bind(GrpcConfig::hostProperty)
}

class AppConfigView : View("Rotation Task Configuration") {
    companion object {
        val LOG: Logger = LogManager.getLogger(AppConfigView::class.qualifiedName)
    }

    override val scope = super.scope as AppConfig

    val isStreaming = SimpleBooleanProperty(false)

    private val connectionAttemptStatus: TaskStatus by inject()

    private val grpcConfigModel = GrpcConfigModel(scope.grpcConfig)
    private val bciConfigModel = BCIConfigModel(scope.bciConfig)
    private val ioConfigModel = IOConfigModel(scope.ioConfig)

    var connectButton: Button by singleAssign()

    override val root = borderpane {}

    init {
        with(root) {
            center {
                form {
                    fieldset("Network Config") {
                        field("Host") {
                            textfield(grpcConfigModel.host) {
                                disableWhen(isStreaming)
                                // TODO: Maybe add validation?
                            }
                        }
                        field("Port") {
                            textfield(grpcConfigModel.port) {
                                disableWhen(isStreaming)
                                filterInput { it.controlNewText.isInt() }
                                // Somehow putting Integer value directly mess up the format,
                                // so we need to provide formatter.
                                textFormatter = TextFormatter(
                                    IntegerStringConverter(),
                                    grpcConfigModel.port.value.toInt()
                                )
                            }
                        }
                    }
                    fieldset("BCI Config") {
                        field("Serial Port") {
                            combobox(bciConfigModel.serialPort) {
                                disableWhen(isStreaming)
                                items = FXCollections.observableArrayList(getPorts())
                                if (value !in items) value = items[0]
                            }
                        }
                        field("Sample Rate") {
                            combobox(bciConfigModel.sampleRate) {
                                disableWhen(isStreaming)
                                items =
                                    FXCollections.observableArrayList(SampleRate.values().map {
                                        it.toLong().toInt()
                                    }.asReversed())
                                if (value !in items) value = items[0]
                            }
                        }
                        field("Number of Channels") {
                            combobox(bciConfigModel.numChannels) {
                                disableWhen(isStreaming)
                                items = FXCollections.observableArrayList(listOf(8, 16))
                                value = items[0]
                            }
                        }
                    }
                    fieldset("") {
                        field("Save directory") {
                            button(ioConfigModel.saveDir) {
                                disableWhen(isStreaming)
                                action {
                                    chooseDirectory(
                                        title = "Select directory to save data",
                                        initialDirectory = if (this.text.isNotEmpty()) File(this.text) else null,
                                        owner = this@AppConfigView.currentWindow
                                    )?.path?.let { ioConfigModel.saveDir.value = it }
                                }
                            }
                        }
                    }
                    buttonbar {
                        button("Reset") {
                            enableWhen(isStreaming.not() and (grpcConfigModel.dirty or bciConfigModel.dirty or ioConfigModel.dirty))
                            action { rollback() }
                        }
                        button("Apply") {
                            enableWhen(isStreaming.not() and (grpcConfigModel.dirty or bciConfigModel.dirty or ioConfigModel.dirty))
                            action { commit(); scope.save() }
                        }
                        connectButton = button() {
                            isWrapText = true
                            textProperty().bind(stringBinding(textProperty(), isStreaming) {
                                if (isStreaming.value) "Disconnect" else "Connect"
                            })
                        }
                    }
                }
            }
            bottom {
                hbox(4) {
                    progressindicator {
                        visibleWhen { connectionAttemptStatus.running }
                        setPrefSize(18.0, 18.0)
                    }
                    label(connectionAttemptStatus.message)
                }
            }
        }
        isStreaming.set(false)
    }

    private fun getPorts(): ArrayList<String> {
        val values = arrayListOf(AppConfig.CYTON_MOCK_PORT);
        for (port: SerialPort in SerialPort.getCommPorts()) {
            values.add(port.systemPortName)
        }
        return values
    }

    private fun rollback() {
        bciConfigModel.rollback()
        grpcConfigModel.rollback()
        ioConfigModel.commit()
    }

    fun commit() {
        bciConfigModel.commit()
        grpcConfigModel.commit()
        ioConfigModel.commit()
    }

    // Get the current input values
    val bciConfig: BCIConfig
        get() = BCIConfig(
            bciConfigModel.serialPort.value,
            bciConfigModel.sampleRate.value.toInt(),
            bciConfigModel.numChannels.value.toInt()
        )

    // Get the current input values
    val networkConfig: GrpcConfig
        get() = GrpcConfig(grpcConfigModel.host.value, grpcConfigModel.port.value.toInt())
}
