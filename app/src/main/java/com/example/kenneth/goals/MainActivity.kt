package com.example.kenneth.goals

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        val ADD_GOAL_REQUEST_CODE = 1;
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
                goalItemAdapter.items.removeAt(viewHolder.adapterPosition)
                goalItemAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                SaveUtil.writeGoalList(this@MainActivity, goalItemAdapter.items)
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
        // Save new empty list under key
        SaveUtil.writeGoalList(this, ArrayList())

        // Clear all items from the array list
        goalItemAdapter.items.clear()
        goalItemAdapter.notifyDataSetChanged()
    }

    // TODO: Not working, need to add new items to old list in adapter
    private fun refreshAdapterWithNewItem() {
        // A new item was added
        val tempGoalList = SaveUtil.loadGoalList(this)
        val newGoalItem = tempGoalList[goalItemAdapter.items.size]
        goalItemAdapter.items.add(newGoalItem)

        // We have now added a new goal item so try to refresh the adapter
        goalItemAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != Activity.RESULT_CANCELED) {
            refreshAdapterWithNewItem()
        }

        // Otherwise, we did not add a new goal so we do not need to refresh the adapter data
    }
}
