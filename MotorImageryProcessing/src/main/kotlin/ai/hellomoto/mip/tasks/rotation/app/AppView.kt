package ai.hellomoto.mip.tasks.rotation.app

import ai.hellomoto.mip.Styles
import ai.hellomoto.mip.tasks.rotation.app.fragments.SimpleStatusBar
import ai.hellomoto.mip.tasks.rotation.app.fragments.SimpleTimeSeries
import javafx.geometry.Pos
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.MenuItem
import javafx.scene.control.ToggleButton
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import tornadofx.*

class MenuBarView : View() {
    var quitMenu: MenuItem by singleAssign()
    var configMenu: MenuItem by singleAssign()
    var startMenu: CheckMenuItem by singleAssign()
    var showBCIPlotMenu: MenuItem by singleAssign()

    override val root = vbox {
        menubar {
            menu {
                text = "Rotation Task"
                configMenu = item("Configure")
                startMenu = checkmenuitem("Start Streaming") {
                    textProperty().stringBinding(selectedProperty()) {
                        if (isSelected) "Stop Streaming" else "Start Streaming"
                    }
                }
                quitMenu = item("Quit")
                configMenu.disableProperty().bind(startMenu.selectedProperty())
            }
            menu {
                text = "View"
                showBCIPlotMenu = item("Hide BCI Controller")
            }
        }
    }
}

class RotationView : View() {
    var image: ImageView by singleAssign()
    val chart = SimpleTimeSeries(hideAxis = false)

    override val root = vbox {
        image = imageview("rotation_task/ic_launcher_round.png") {
            // Normally, VBox automatically resize the children, but ImageView break this.
            // So we manually set the image height to occupy the half of the remaining height of window height
            isPreserveRatio = true
            fitHeightProperty().bind(this@vbox.heightProperty().divide(2.0))
            vboxConstraints {
                alignment = Pos.CENTER
            }
        }
        chart.root.apply {
            isLegendVisible = false
            vboxConstraints {
                vgrow = Priority.ALWAYS
                alignment = Pos.CENTER
            }
            this@vbox.children.add(this)
        }
        addClass(Styles.debug1)
        children.addClass(Styles.debug2)
    }
}

class AppView : View() {
    val menuBar: MenuBarView by inject()
    val rotationView: RotationView by inject()
    val statusBar = SimpleStatusBar()

    override val root = borderpane {
        center = rotationView.root.apply {
            // prevents center content from hiding bottom content
            minHeight = 0.0
        }
        top = menuBar.root
        bottom = statusBar.root
    }
}
