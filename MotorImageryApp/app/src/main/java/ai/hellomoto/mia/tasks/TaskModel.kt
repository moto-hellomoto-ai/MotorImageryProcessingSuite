package ai.hellomoto.mia.tasks

import java.io.Serializable

data class TaskModel(
    val name: String,
    val description: String,
    var text: String = ""
) : Serializable
