package ai.hellomoto.mip.tasks.rotation.app

import ai.hellomoto.mip.openbci.SampleRate
import com.fazecast.jSerialComm.SerialPort
import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import tornadofx.*

class AppConfigView : View("Rotation Task Configuration") {
    override val scope = super.scope as AppConfig

    private var host: TextField by singleAssign()
    private var port: TextField by singleAssign()
    private var sampleRate: ComboBox<Int> by singleAssign()
    private var numChannels: ComboBox<Int> by singleAssign()
    private var serialPort: ComboBox<String> by singleAssign()


    override val root = form {
        fieldset("Network Config") {
            field("Host") {
                // TODO maybe add host pattern verification?
                host = textfield { }
            }
            field("Port") {
                port = textfield {
                    filterInput { it.controlNewText.isInt() }
                }
            }
        }
        fieldset("BCI Config") {
            field("Serial Port") {
                serialPort = combobox {}
            }
            field("Sample Rate") {
                sampleRate = combobox {
                    items =
                        FXCollections.observableArrayList(SampleRate.values().map { it.toLong().toInt() }.asReversed())
                    value = items[0]
                }
            }
            field("Number of Channels") {
                numChannels = combobox {
                    items = FXCollections.observableArrayList(listOf(8, 16))
                    value = items[0]
                }
            }
        }
        buttonbar {
            button("Apply") {
                action { setFields() }
            }
            button("Reset") {
                action { resetFields() }
            }
        }
    }
    
    private fun getPorts(): ArrayList<String> {
        val values = arrayListOf(AppConfig.CYTON_MOCK_PORT);
        for (port:SerialPort in SerialPort.getCommPorts()) {
            values.add(port.systemPortName)
        }
        return values
    }

    fun resetFields() {
        host.text = scope.grpcHost
        port.text = scope.grpcPort.toString()
        if (scope.bciSampleRate in sampleRate.items) {
            sampleRate.value = scope.bciSampleRate
        }
        if (scope.bciNumChannels in numChannels.items) {
            numChannels.value = scope.bciNumChannels
        }
        serialPort.apply {
            items = FXCollections.observableArrayList(getPorts())
            value = if (scope.bciSerialPort in items) scope.bciSerialPort else items[0]
        }
    }

    private fun setFields() {
        scope.grpcHost = host.text
        scope.grpcPort = port.text.toInt()
        scope.bciSerialPort = serialPort.value
        scope.bciSampleRate = sampleRate.value
        scope.bciNumChannels = numChannels.value
    }

    override fun onUndock() {
        super.onUndock()
        scope.save()
    }
}
