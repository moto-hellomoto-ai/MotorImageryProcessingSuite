package ai.hellomoto.mip.tasks.rotation.app;

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.Window
import tornadofx.*

class GrpcConfig(host: String = "0.0.0.0", port: Int = 59898) {
    val hostProperty = SimpleStringProperty(this, "host", host)
    var host: String by hostProperty

    val portProperty = SimpleIntegerProperty(this, "port", port)
    var port: Int by portProperty
}

class BCIConfig(serialPort: String, sampleRate: Int = 250, numChannels: Int = 8) {
    val serialPortProperty = SimpleStringProperty(this, "port", serialPort)
    var serialPort: String by serialPortProperty

    val sampleRateProperty = SimpleIntegerProperty(this, "sampleRate", sampleRate)
    var sampleRate: Int by sampleRateProperty

    val numChannelsProperty = SimpleIntegerProperty(this, "numChannels", numChannels)
    var numChannels: Int by numChannelsProperty
}

class IOConfig(saveDir: String="") {
    val saveDirProperty = SimpleStringProperty(saveDir)
    var saveDir: String by saveDirProperty
}

class AppConfig(val config: ConfigProperties) : Scope() {
    companion object {
        const val CYTON_MOCK_PORT = "MOCK"
        // Keys for storing config
        private const val GRPC_HOST = "grpc.host"
        private const val GRPC_PORT = "grpc.port"
        private const val BCI_PORT = "bci.serial_port"
        private const val BCI_SAMPLE_RATE = "bci.sample_rate"
        private const val BCI_NUM_CHANNELS = "bci.num_channels"
        private const val SAVE_DIR = "save_dir"
    }

    val grpcConfig = GrpcConfig(
        host = config.string(GRPC_HOST, "0.0.0.0"),
        port = config.int(GRPC_PORT, 59898)
    )

    val bciConfig = BCIConfig(
        serialPort = config.string(BCI_PORT, ""),
        sampleRate = config.int(BCI_SAMPLE_RATE, 250),
        numChannels = config.int(BCI_NUM_CHANNELS, 8)
    )

    val ioConfig = IOConfig(
        saveDir = config.string(SAVE_DIR, System.getProperty("user.home"))
    )

    fun save() {
        with(config) {
            set(GRPC_HOST to grpcConfig.host)
            set(GRPC_PORT to grpcConfig.port)
            set(BCI_PORT to bciConfig.serialPort)
            set(BCI_SAMPLE_RATE to bciConfig.sampleRate)
            set(BCI_NUM_CHANNELS to bciConfig.numChannels)
            set(SAVE_DIR to ioConfig.saveDir)
            save()
        }
    }

    fun storeWindowPosition(name: String, window: Window) {
        with(config) {
            set("${name}.window.x" to window.x)
            set("${name}.window.y" to window.y)
            set("${name}.window.w" to window.width)
            set("${name}.window.h" to window.height)
            save()
        }
    }

    fun restoreWindowPosition(name: String, window: Window) {
        config.double("${name}.window.x")?.let { window.x = it }
        config.double("${name}.window.y")?.let { window.y = it }
        config.double("${name}.window.w")?.let { window.width = it }
        config.double("${name}.window.h")?.let { window.height = it }
    }
}
