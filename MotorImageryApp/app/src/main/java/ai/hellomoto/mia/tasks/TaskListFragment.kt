package ai.hellomoto.mia.tasks

import ai.hellomoto.mia.R
import ai.hellomoto.mia.databinding.RecyclerItemTaskModelBinding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TaskListFragment : Fragment() {

    private lateinit var names: Array<String>
    private lateinit var descriptions: Array<String>
    private lateinit var listener: OnTaskSelected

    companion object {
        fun newInstance(): TaskListFragment {
            return TaskListFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnTaskSelected) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement OnTaskSelected.")
        }

        // Get task names and descriptions.
        val resources = context.resources
        names = resources.getStringArray(R.array.task_names)
        descriptions = resources.getStringArray(R.array.task_descriptions)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(
            R.layout.fragment_task_list, container, false
        )
        val activity = activity as Context
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(activity, 1)
        recyclerView.adapter = taskListAdapter(activity)
        return view
    }

    internal inner class taskListAdapter(context: Context) : RecyclerView.Adapter<ViewHolder>() {

        private val layoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val recyclertaskModelBinding =
                RecyclerItemTaskModelBinding.inflate(layoutInflater, viewGroup, false)
            return ViewHolder(recyclertaskModelBinding.root, recyclertaskModelBinding)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val task = TaskModel(names[position], descriptions[position])
            viewHolder.setData(task)
            viewHolder.itemView.setOnClickListener { listener.onTaskSelected(viewHolder.itemView, task) }
        }

        override fun getItemCount() = names.size
    }

    internal inner class ViewHolder constructor(
        itemView: View,
        private val recyclerItemtaskListBinding: RecyclerItemTaskModelBinding
    ) :
        RecyclerView.ViewHolder(itemView) {

        fun setData(taskModel: TaskModel) {
            recyclerItemtaskListBinding.taskModel = taskModel
        }
    }

    interface OnTaskSelected {
        fun onTaskSelected(view: View, task: TaskModel)
    }

}
