package ai.hellomoto.mip

import ai.hellomoto.mip.settings.board_config.BoardConfigFragment
import ai.hellomoto.mip.tasks.rotation_task.RotationTaskProcessorView
import javafx.application.Platform
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import tornadofx.*

class MainView : View() {
    override val root: AnchorPane by fxml()
    private val menuBar:MenuBar by fxid("menuBar")
    private val mSettingsBoard:MenuItem by fxid("mSettingsBoard")
    private val mTasksRotationRecord:MenuItem by fxid("mTasksRotationRecord")

    init {
        Platform.setImplicitExit(true)
        menuBar.prefWidthProperty().bind(root.widthProperty())
        mTasksRotationRecord.action { replaceWith<RotationTaskProcessorView>() }
        mSettingsBoard.action { find<BoardConfigFragment>().openModal() }
    }
}
