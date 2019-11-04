package ai.hellomoto.mip.tasks.rotation.app.fragments

import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.*

class SimpleStatusBar : Fragment() {
    private var indicator: Circle by singleAssign()
    private var messageText: Label by singleAssign()

    private var currentColor = Color.GRAY

    override val root = hbox {
        messageText = label {}
        region {
            hboxConstraints {
                hgrow = Priority.ALWAYS
            }
        }
        indicator = circle(0, 0, 6) {
            fill = currentColor
            hboxConstraints {
                marginLeft = 4.0
                marginRight = 2.0
                alignment = Pos.CENTER
                vgrow = Priority.ALWAYS
            }
        }
    }

    var message: String
        get() = ""
        set(value) = Platform.runLater { messageText.text = value }

    fun inactive() { changeColor(Color.GRAY) }

    fun started() { changeColor(Color.GREEN) }

    fun active() { changeColor(Color.BLUE) }

    fun error() { changeColor(Color.RED) }

    fun completed() { changeColor(Color.AQUA) }

    private fun changeColor(color:Color) {
        if (currentColor != color) {
            currentColor = color
            Platform.runLater {
                indicator.fill = color
            }
        }
    }

    init {
        this.message = "${this}"
    }
}