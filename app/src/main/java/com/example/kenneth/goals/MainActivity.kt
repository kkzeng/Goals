package com.example.kenneth.goals

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.design.widget.Snackbar
import android.view.View
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        val ADD_GOAL_REQUEST_CODE = 1

        // Temporarily hold removed item for potential 'Undo' action
        var removedPos: Int = -1
        var removedItem: GoalItem? = null

        fun removeGoalItem(goalItemAdapter: GoalItemAdapter, toRemove: RecyclerView.ViewHolder, context: Context) {

            // Save the item first for potential 'Undo'
            removedPos = toRemove.adapterPosition
            removedItem = goalItemAdapter.items[removedPos]

            // Remove item from data set
            goalItemAdapter.items.removeAt(toRemove.adapterPosition)
            goalItemAdapter.notifyItemRemoved(toRemove.adapterPosition)

            // Display snackbar
            Snackbar.make(toRemove.itemView, removedItem!!.title, Snackbar.LENGTH_INDEFINITE).setAction("UNDO") {
                goalItemAdapter.items.add(removedPos, removedItem!!)
                goalItemAdapter.notifyItemInserted(removedPos)

                // UI tweak: If we removed the first item, scroll back to the top, to avoid weird
                // cut off item at the top.if(removedPos == 0) {
                    goalItemAdapter.recyclerView.scrollToPosition(0)
            }.show()

            // Save
            SaveUtil.writeGoalList(context, goalItemAdapter.items)

        }

    }
    // RecyclerView for list of goals
    lateinit var goalRecyclerView: RecyclerView
    lateinit var goalItemAdapter: GoalItemAdapter

    // List of goals
    lateinit var goalList: ArrayList<GoalItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Set up recycler view
        goalRecyclerView = findViewById(R.id.goalListRecyclerView)
        goalRecyclerView.setHasFixedSize(true)
        goalRecyclerView.layoutManager = LinearLayoutManager(this)

        goalList = SaveUtil.loadGoalList(this)
        goalItemAdapter = GoalItemAdapter(this, goalList)
        goalRecyclerView.adapter = goalItemAdapter

        // Implement gestures like drag and swipe
        val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, dragged: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val posDragged = dragged.adapterPosition
                val posTarget = target.adapterPosition

                Collections.swap(goalItemAdapter.items, posDragged, posTarget)
                goalItemAdapter.notifyItemMoved(posDragged, posTarget)

                SaveUtil.writeGoalList(this@MainActivity, goalItemAdapter.items)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                MainActivity.removeGoalItem(goalItemAdapter, viewHolder, this@MainActivity)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(goalRecyclerView)
    }

    // Handler method for adding goals
    fun openAddGoalActivity(@Suppress("UNUSED_PARAMETER") v: View) {
        // Open an activity to add goals
        val intent = Intent(this, AddGoalActivity::class.java)
        startActivityForResult(intent, ADD_GOAL_REQUEST_CODE)
    }

    // Handler method for clearing all goals
    fun clearAllGoals(@Suppress("UNUSED_PARAMETER") v: View) {
        // Clear all items from the array list
        goalItemAdapter.items.clear()
        goalItemAdapter.notifyDataSetChanged()

        // Display a Snackbar asking if the user wishes to 'Undo' the delete

        // If user does not choose to undo then
        // save new empty list under key
        SaveUtil.writeGoalList(this, ArrayList())

        /*// Otherwise, load it back
        if(Undo) {
            val oldSavedList = SaveUtil.loadGoalList(this)
            goalItemAdapter.items.addAll(oldSavedList)
        }*/
    }

    // Handler method for sorting by priority
    fun sortByPriority(@Suppress("UNUSED_PARAMETER") v: View) {
        goalItemAdapter.items.sortWith(GoalItemComparator())
        goalItemAdapter.notifyDataSetChanged()
        SaveUtil.writeGoalList(this, goalItemAdapter.items)
    }

    // TODO: Not working, need to add new items to old list in adapter
    private fun refreshAdapterWithNewItem() {
        // A new item was added
        val tempGoalList = SaveUtil.loadGoalList(this)
        val newGoalItem = tempGoalList[0] // Item should be at the front of the list
        goalItemAdapter.items.add(0, newGoalItem)

        // We have now added a new goal item so try to refresh the adapter
        goalItemAdapter.notifyItemInserted(0)

        // Adapter position reset
        goalRecyclerView.scrollToPosition(0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != Activity.RESULT_CANCELED) {
            refreshAdapterWithNewItem()
        }

        // Otherwise, we did not add a new goal so we do not need to refresh the adapter data
    }

    internal class GoalItemComparator : Comparator<GoalItem> {
        override fun compare(c1: GoalItem, c2: GoalItem): Int {

            // Sorts in descending order of priority
            return c2.priority.compareTo(c1.priority)
        }
    }
}
