package ai.hellomoto.mip.settings.board_config

import ai.hellomoto.mip.openbci.Cyton
import ai.hellomoto.mip.openbci.OperationResult
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.layout.*
import org.jetbrains.anko.doAsync
import tornadofx.*


class BoardConfigFragment : Fragment("Board Configuration") {
    data class DefaultConfigs(
        val serialPort:String
    )

    class StoredConfig:ItemViewModel<DefaultConfigs>() {
        val serialPort = bind { SimpleStringProperty(item?.serialPort, "", config.string("serialPort")) }
    }

    override val root:VBox by fxml()
    private val storedConfig = StoredConfig()

    private val toolbar: ToolBar by fxid("toolbar")
    private val infoText: TextArea by fxid("infoText")

    // Serial port selector
    private val portSelector: ComboBox<String> by fxid("portSelector")
    private val connectButton: Button by fxid("connectButton")

    // Board config
    private val configPane: AnchorPane by fxid("configPane")
    private val sampleRateSelector:ComboBox<String> by fxid("sampleRateSelector")

    init {
        toolbar.prefWidthProperty().bind(root.widthProperty())
        portSelector.items = FXCollections.observableArrayList(getPorts())
        if (storedConfig.serialPort.value in portSelector.items) {
            portSelector.value = storedConfig.serialPort.value
        }
        connectButton.action { doAsync { connectButtonAction() } }
        sampleRateSelector.items = FXCollections.observableArrayList(getSampleRates())
    }

    private fun connectButtonAction() {
        root.isDisable = true
        println("Connecting ${portSelector.value}")
        val cyton = Cyton(portSelector.value)
        try {
            initConfigPane(cyton)
        } finally {
            root.isDisable = false
            cyton.close()
        }
    }

    private fun initConfigPane(cyton:Cyton) {
        val result = cyton.init()
        infoText.text = result.message
        if (result is OperationResult.Success) {
            storeDefaultPort()
            configPane.isDisable = false
            // Check samplerate and set in combobox
            if (cyton.sampleRate != null) {
                Platform.runLater {
                    sampleRateSelector.value = cyton.sampleRate.toString()
                }
            }
        }
    }

    private fun storeDefaultPort() {
        with(storedConfig.config) {
            set("serialPort" to portSelector.value)
            save()
        }
    }
}
