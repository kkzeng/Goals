package com.example.kenneth.goals

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView


class GoalItemAdapter(val context: Context, val items : ArrayList<GoalItem>) : RecyclerView.Adapter<GoalItemAdapter.GoalItemViewHolder>() {

    companion object {
        const val GOAL_ITEM = 0
        const val ADD_BUTTON = 1
    }

    lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(rv: RecyclerView) {
        super.onAttachedToRecyclerView(rv)
        recyclerView = rv
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): GoalItemViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View
        view = if (viewType == GOAL_ITEM) {
            inflater.inflate(R.layout.list_main, viewGroup, false)
        } else {
            inflater.inflate(R.layout.list_add_button, viewGroup, false)
        }
        return GoalItemViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: GoalItemViewHolder, position: Int) {
        // If it is the last item which is the add button
        if(position == items.size) {
            return
        }

        // Otherwise, load the correct information
        val goalItem = items[position]
        viewHolder.goalTitleView!!.text = goalItem.title
        viewHolder.goalDescView!!.text = goalItem.desc
        viewHolder.goalPriorityView!!.text = goalItem.priority.toString()
        viewHolder.goalCheckBox!!.isChecked = false

        // Set an listener for the checkbox to remove the item
        // Interesting note: When using onCheckedChangeListener, when my view was
        // recreated after choosing to Undo the delete, I set the checkbox to Unchecked
        // and this triggers the listener which results in some unpredictable behaviour/crashes
        // Must check for isChecked to get the desired behaviour
        viewHolder.goalCheckBox.setOnCheckedChangeListener { _,isChecked ->
            if(isChecked) {
                MainActivity.removeGoalItem(this, viewHolder, this@GoalItemAdapter.context)
            }
        }

        // Sets long press listener for editing goals
        viewHolder.container!!.setOnClickListener {
            (context as MainActivity).openEditGoalActivity(viewHolder.adapterPosition)
        }

        // Get a val copy of the var so it is not mutable
        val drawable = goalItem.image

        if (drawable != null) {
            viewHolder.goalImageView!!.setImageDrawable(context.getDrawable(drawable))
        }
        else {
            // Default icon
            viewHolder.goalImageView!!.setImageDrawable(context.getDrawable(R.drawable.defaultgoalicon))
        }

        //
    }

    override fun getItemCount(): Int {
        return items.size + 1 // One item for the add button at the end
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == items.size) ADD_BUTTON else GOAL_ITEM
    }

    inner class GoalItemViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val container: RelativeLayout? = view.findViewById(R.id.container)
        val goalImageView: ImageView? = view.findViewById(R.id.goal_image)
        val goalTitleView: TextView? = view.findViewById(R.id.goal_title)
        val goalDescView: TextView? = view.findViewById(R.id.goal_desc)
        val goalPriorityView: TextView? = view.findViewById(R.id.goal_prio)
        val goalCheckBox: CheckBox? = view.findViewById(R.id.goal_check_box)
    }
}