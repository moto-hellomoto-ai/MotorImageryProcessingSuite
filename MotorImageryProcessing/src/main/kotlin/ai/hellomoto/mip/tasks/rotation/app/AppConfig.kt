package ai.hellomoto.mip.tasks.rotation.app;

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.Window
import tornadofx.*

class AppConfig(val config: ConfigProperties): Scope() {
    companion object {
        const val CYTON_MOCK_PORT = "MOCK"
        // Keys for storing config
        private const val GRPC_HOST = "grpc.host"
        private const val GRPC_PORT = "grpc.port"
        private const val BCI_PORT = "bci.serial_port"
        private const val BCI_SAMPLE_RATE = "bci.sample_rate"
        private const val BCI_NUM_CHANNELS = "bci.num_channels"
    }

    private val hostProperty = SimpleStringProperty(config.string(GRPC_HOST, "0.0.0.0"))
    var grpcHost: String by hostProperty

    private val portProperty = SimpleIntegerProperty(config.int(GRPC_PORT, 59898))
    var grpcPort: Int by portProperty

    private val serialPortProperty = SimpleStringProperty(config.string(BCI_PORT, ""))
    var bciSerialPort: String by serialPortProperty

    private val sampleRateProperty = SimpleIntegerProperty(config.int(BCI_SAMPLE_RATE, 250))
    var bciSampleRate: Int by sampleRateProperty

    private val numChannelsProperty = SimpleIntegerProperty(config.int(BCI_NUM_CHANNELS, 8))
    var bciNumChannels: Int by numChannelsProperty

    fun save() {
        with(config) {
            set(GRPC_HOST to grpcHost)
            set(GRPC_PORT to grpcPort)
            if (bciSerialPort != CYTON_MOCK_PORT) {
                set(BCI_PORT to bciSerialPort)
            }
            set(BCI_SAMPLE_RATE to bciSampleRate)
            set(BCI_NUM_CHANNELS to bciNumChannels)
            save()
        }
    }

    fun storeWindowPosition(name: String, window : Window) {
        with(config) {
            set("${name}.window.x" to window.x)
            set("${name}.window.y" to window.y)
            set("${name}.window.w" to window.width)
            set("${name}.window.h" to window.height)
            save()
        }
    }

    fun restoreWindowPosition(name: String, window : Window) {
        config.double("${name}.window.x")?.let { window.x = it }
        config.double("${name}.window.y")?.let { window.y = it }
        config.double("${name}.window.w")?.let { window.width = it }
        config.double("${name}.window.h")?.let { window.height = it }
    }
}
