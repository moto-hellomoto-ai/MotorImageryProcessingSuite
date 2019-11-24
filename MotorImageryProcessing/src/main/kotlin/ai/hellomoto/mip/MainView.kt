package ai.hellomoto.mip

import ai.hellomoto.mip.tasks.rotation.app.AppView
import tornadofx.*

class MainView : View() {
    override val root = borderpane {
        prefHeight = 400.0
        prefWidth = 600.0
        top = menubar {
            menu("Tasks") {
                item("Rotation") {
                    action {
                        close()
                        find<AppView>().openWindow(
                            block = true, escapeClosesWindow = false)
                    }
                }
            }
        }
    }
}
