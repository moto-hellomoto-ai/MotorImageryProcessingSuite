package ai.hellomoto.mip

import ai.hellomoto.mip.settings.board_config.BoardConfigFragment
import ai.hellomoto.mip.tasks.rotation_task.RotationTaskProcessorView
import tornadofx.*

class MainView : View() {
    override val root = borderpane {
        prefHeight = 400.0
        prefWidth = 600.0
        top = menubar {
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
