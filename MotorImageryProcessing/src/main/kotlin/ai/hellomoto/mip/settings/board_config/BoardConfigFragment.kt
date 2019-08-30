package ai.hellomoto.mip.settings.board_config

import ai.hellomoto.mip.openbci.Cyton
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import org.jetbrains.anko.doAsync
import tornadofx.*

data class DefaultConfigs(
    val serialPort:String
)

class DefaultConfigsModel : ItemViewModel<DefaultConfigs>() {
    val serialPort = bind { SimpleStringProperty(item?.serialPort, "", config.string("serialPort")) }
}


class BoardConfigFragment : Fragment("Board Configuration") {
    override val root: AnchorPane by fxml()
    private val toolbar: ToolBar by fxid("toolbar")
    private val portSelector: ComboBox<String> by fxid("portSelector")
    private val connectButton: Button by fxid("connectButton")
    private val configPane: AnchorPane by fxid("configPane")
    private val firmwareMessage: TextArea by fxid("firmwareMessage")
    private val sampleRateSelector:ComboBox<String> by fxid("sampleRateSelector")

    private val defaultConfig = DefaultConfigsModel()
    private var board:Cyton? = null

    init {
        toolbar.prefWidthProperty().bind(root.widthProperty())
        portSelector.items = FXCollections.observableArrayList(getPorts())
        if (defaultConfig.serialPort.value in portSelector.items) {
            portSelector.value = defaultConfig.serialPort.value
        }
        configPane.isDisable = true
        connectButton.action { doAsync { connectButtonAction() } }
        firmwareMessage.prefWidthProperty().bind(root.widthProperty() - 16 )
        firmwareMessage.isEditable = false
        sampleRateSelector.items = FXCollections.observableArrayList(getSampleRates())
    }

    private fun connectButtonAction() {
        toolbar.isDisable = true
        val cyton = Cyton(portSelector.value)
        try {
            if (true /*cyton.firmwareVersion != ""*/) {
                initConfigPane(cyton)
                storeDefaultPort()
            }
        } finally {
            toolbar.isDisable = false
            cyton.close()
        }
    }

    private fun initConfigPane(cyton:Cyton) {
        // Enable config pane.
        configPane.isDisable = false
        firmwareMessage.text = """OpenBCI V3 8-16 channel
On Board ADS1299 Device ID: 0x3E
On Daisy ADS1299 Device ID: 0x3E
LIS3DH Device ID: 0x33
Firmware: v3.1.1
$$$"""
        // Check samplerate and set in combobox
        // cyton.sampleRate

    }

    private fun storeDefaultPort() {
        with(defaultConfig.config) {
            set("serialPort" to portSelector.value)
            save()
        }
    }
}
