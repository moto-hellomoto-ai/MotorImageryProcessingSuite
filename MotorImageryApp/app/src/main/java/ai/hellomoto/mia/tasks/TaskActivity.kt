package ai.hellomoto.mia.tasks

import ai.hellomoto.mia.R
import ai.hellomoto.mia.tasks.rotation_task.RotationFragment
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class TaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        val task = intent.getStringExtra("TASK_NAME")
        val fragment: Fragment = when (task) {
            getString(R.string.task_rotary_name) -> RotationFragment()
            else -> {
                val message = "Task \"$task\" is not implemented."
                val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                toast.show()
                return
            }
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.task_layout, fragment, task)
            .commit()
    }
}
