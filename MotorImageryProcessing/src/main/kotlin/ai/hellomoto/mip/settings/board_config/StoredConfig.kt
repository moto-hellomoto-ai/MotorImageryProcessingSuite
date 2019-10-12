package ai.hellomoto.mip.settings.board_config

import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel

data class DefaultConfigs(
    val serialPort: String,
    val sampleRate: String
)

class StoredConfig : ItemViewModel<DefaultConfigs>() {
    val serialPort = bind { SimpleStringProperty(item?.serialPort, "Serial Port", config.string("serialPort")) }
    val sampleRate = bind { SimpleStringProperty(item?.sampleRate, "Sample Rate", config.string("sampleRate")) }
}
