package ai.hellomoto.mip

import ai.hellomoto.mip.tasks.rotation_task.RotationTaskProcessorView
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import tornadofx.*

class MainView : View() {
    override val root: AnchorPane by fxml()
    private val menuBar:MenuBar by fxid("menuBar")
    private val mRotationTaskRecord:MenuItem by fxid("rotationTaskRecord")

    init {
        menuBar.prefWidthProperty().bind(root.widthProperty())
        mRotationTaskRecord.action { replaceWith<RotationTaskProcessorView>() }
    }
}
