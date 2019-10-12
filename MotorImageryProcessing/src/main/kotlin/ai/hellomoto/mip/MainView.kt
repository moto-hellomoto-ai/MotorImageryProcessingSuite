package ai.hellomoto.mip

import ai.hellomoto.mip.settings.board_config.BoardConfigFragment
import ai.hellomoto.mip.tasks.rotation_task.RotationTaskProcessorView
import tornadofx.*

class MainView : View() {
    override val root = vbox {
        prefHeight = 400.0
        prefWidth = 600.0
        menubar {
            menu("Settings") {
                item("Board") {
                    action { find<BoardConfigFragment>().openModal() }
                }
            }
            menu("Tasks") {
                item("Rotation") {
                    action { replaceWith<RotationTaskProcessorView>() }
                }
            }
        }
    }
}
