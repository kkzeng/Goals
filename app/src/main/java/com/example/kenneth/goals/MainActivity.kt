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
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.example.kenneth.goals.GoalItemAdapter.Companion.ADD_BUTTON
import com.example.kenneth.goals.ModifyGoalActivity.Companion.ADAPTER_POS_BUNDLE_KEY
import com.example.kenneth.goals.ModifyGoalActivity.Companion.ADD_GOAL_REQUEST_CODE
import com.example.kenneth.goals.ModifyGoalActivity.Companion.EDIT_GOAL_REQUEST_CODE
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
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
            Snackbar.make(toRemove.itemView, "${removedItem!!.title} deleted", Snackbar.LENGTH_INDEFINITE)
                .setAction("UNDO") {
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
        val itemTouchHelper = ItemTouchHelper(GoalItemTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(goalRecyclerView)
    }

    // Handler method for adding goals
    fun openAddGoalActivity(@Suppress("UNUSED_PARAMETER") v: View) {
        // Open an activity to add goals
        val intent = Intent(this, ModifyGoalActivity::class.java)
        startActivityForResult(intent, ADD_GOAL_REQUEST_CODE)
    }

    fun openEditGoalActivity(adapterPosition: Int) {
        // Get relevant information
        val goalItem = goalItemAdapter.items[adapterPosition]

        val title = goalItem.title
        val desc = goalItem.desc
        val priority = goalItem.priority

        // Open an activity to add goals
        val intent = ModifyGoalActivity.createEditIntent(this, title, desc, priority, adapterPosition)
        startActivityForResult(intent, EDIT_GOAL_REQUEST_CODE)
    }

    // Handler method for clearing all goals
    fun clearAllGoals(@Suppress("UNUSED_PARAMETER") v: View) {
        val goalItemListCopy = goalItemAdapter.items.clone() as ArrayList<GoalItem>
        var undoChosen = false
        // Clear all items from the array list
        goalItemAdapter.items.clear()
        goalItemAdapter.notifyDataSetChanged()

        // Display a Snackbar asking if the user wishes to 'Undo' the delete
        Snackbar.make(goalRecyclerView, "Removed all items", Snackbar.LENGTH_SHORT).setAction("UNDO") {
            goalItemAdapter.items.addAll(goalItemListCopy)
            goalItemAdapter.notifyDataSetChanged()
            undoChosen = true
        }.show()

        // If user does not choose to undo then
        // save new empty list under key
        if(!undoChosen) {
            SaveUtil.writeGoalList(this, ArrayList())
        }
    }

    // Handler method for sorting by priority
    fun sortByPriority(@Suppress("UNUSED_PARAMETER") v: View) {
        goalItemAdapter.items.sortWith(GoalItemComparator())
        goalItemAdapter.notifyDataSetChanged()
        SaveUtil.writeGoalList(this, goalItemAdapter.items)

        // Show user that the action was performed
        Toast.makeText(this, "Sorted goals by priority", LENGTH_SHORT).show()
    }

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

    private fun refreshDataOnEditedItem(adapterPosition: Int) {
        // An item was edited
        val tempGoalList = SaveUtil.loadGoalList(this)
        val editedGoalItem = tempGoalList[adapterPosition]

        // The edited item was in our saved copy of the list, update this in our live list
        goalItemAdapter.items[adapterPosition] = editedGoalItem

        // Refresh just the data for the one edited item
        goalItemAdapter.notifyItemChanged(adapterPosition)
    }

    @Throws(Exception::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // We cancelled so we do not need to refresh the adapter data
        if(resultCode == Activity.RESULT_CANCELED) return
        if(requestCode == ADD_GOAL_REQUEST_CODE) refreshAdapterWithNewItem()
        if(requestCode == EDIT_GOAL_REQUEST_CODE) {
            // For edits, we always pass back the intent with the extra containing the adapter position
            val adapterPos = data!!.getIntExtra(ADAPTER_POS_BUNDLE_KEY, -1)
            if(adapterPos == -1) throw Exception()
            refreshDataOnEditedItem(adapterPos)
        }
    }

    inner class GoalItemTouchHelperCallback: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, dragged: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val posDragged = dragged.adapterPosition
            val posTarget = target.adapterPosition

            Collections.swap(goalItemAdapter.items, posDragged, posTarget)
            goalItemAdapter.notifyItemMoved(posDragged, posTarget)

            // Scroll to the position that the item was dragged
            recyclerView.smoothScrollToPosition(posTarget)

            SaveUtil.writeGoalList(this@MainActivity, goalItemAdapter.items)
            return false
        }

        override fun canDropOver(
            recyclerView: RecyclerView,
            current: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return if(recyclerView.adapter!!.getItemViewType(target.adapterPosition) == ADD_BUTTON) false;
            else super.canDropOver(recyclerView, current, target)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            MainActivity.removeGoalItem(goalItemAdapter, viewHolder, this@MainActivity)
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return if (recyclerView.adapter!!.getItemViewType(viewHolder.adapterPosition) == ADD_BUTTON) {
                ItemTouchHelper.Callback.makeMovementFlags(0, 0)
            } else if (viewHolder.adapterPosition == recyclerView.adapter!!.itemCount - 2 || viewHolder.adapterPosition == 0) {
                // If it is the last item that is not the button make it so that it cannot be dragged downwards
                // If it is the first item, it cannot be dragged upwards
                val itemCount = recyclerView.adapter!!.itemCount;
                when {
                    // ItemCount == 2, means theres only 1 goal item
                    itemCount == 2 -> ItemTouchHelper.Callback.makeMovementFlags(
                        0,
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    )
                    viewHolder.adapterPosition == itemCount - 2 -> ItemTouchHelper.Callback.makeMovementFlags(
                        ItemTouchHelper.UP,
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    )
                    viewHolder.adapterPosition == 0 -> ItemTouchHelper.Callback.makeMovementFlags(
                        ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    )
                    else -> super.getDragDirs(recyclerView, viewHolder)
                }
            } else {
                super.getMovementFlags(recyclerView, viewHolder)
            }
        }
    }
}
