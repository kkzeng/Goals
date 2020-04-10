package com.example.kenneth.goals

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.TextView

/**
 * Activity used to add new goals or to modify existing ones
 */
class ModifyGoalActivity : AppCompatActivity() {

    companion object {
        const val ADD_GOAL_REQUEST_CODE = 1
        const val EDIT_GOAL_REQUEST_CODE = 2

        // Bundle keys for intent
        const val EDIT_MODE_KEY = "EDIT_MODE_KEY"
        const val TITLE_BUNDLE_KEY = "TITLE_BUNDLE_KEY"
        const val DESC_BUNDLE_KEY = "DESC_BUNDLE_KEY"
        const val PRIORITY_BUNDLE_KEY = "PRIORITY_BUNDLE_KEY"
        const val ADAPTER_POS_BUNDLE_KEY = "ADAPTER_POS_BUNDLE_KEY"

        fun createEditIntent(context: Context, title: String, desc: String, priority: Int, adapterPosition: Int): Intent {
            val intent = Intent(context, ModifyGoalActivity::class.java).apply {
                putExtra(EDIT_MODE_KEY, true)
                putExtra(TITLE_BUNDLE_KEY, title)
                putExtra(DESC_BUNDLE_KEY, desc)
                putExtra(PRIORITY_BUNDLE_KEY, priority)
                putExtra(ADAPTER_POS_BUNDLE_KEY, adapterPosition)
            }
            return intent
        }
    }

    var adapterPosition: Int = -1
    var isInEditMode: Boolean = false

    lateinit var goalTitleEditText: EditText
    lateinit var goalDescEditText: EditText
    lateinit var goalPrioSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_goal)

        val priorityDisplay = findViewById<TextView>(R.id.priority_display)

        goalTitleEditText = findViewById(R.id.title_edittext)
        goalDescEditText = findViewById(R.id.description_edittext)
        goalPrioSeekBar = findViewById(R.id.priority_slider)

        goalPrioSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                priorityDisplay.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        isInEditMode = intent.getBooleanExtra(EDIT_MODE_KEY, false)
        if(isInEditMode) loadAndSetInfoForEdit()
        else prepareForAdd()
    }

    fun prepareForAdd() {
        val editButton = findViewById<Button>(R.id.edit_button)
        val addButton = findViewById<Button>(R.id.add_button)
        addButton.visibility = View.VISIBLE
        editButton.visibility = View.GONE
    }

    fun loadAndSetInfoForEdit() {
        val bundle = intent.extras!!
        adapterPosition = bundle.getInt(ADAPTER_POS_BUNDLE_KEY)

        val title = bundle.getString(TITLE_BUNDLE_KEY)
        val desc = bundle.getString(DESC_BUNDLE_KEY)
        val priority = bundle.getInt(PRIORITY_BUNDLE_KEY)

        // Set information for the text fields since it is an edit
        goalTitleEditText.setText(title)
        goalDescEditText.setText(desc)
        goalPrioSeekBar.progress = priority

        // Toggle visibility of edit button and add buttons
        val editButton = findViewById<Button>(R.id.edit_button)
        val addButton = findViewById<Button>(R.id.add_button)
        addButton.visibility = View.GONE
        editButton.visibility = View.VISIBLE
    }

    fun addNewGoalItem(@Suppress("UNUSED_PARAMETER") v: View) {
        val goalList = SaveUtil.loadGoalList(this)

        // Get relevant information from views
        val newItem = GoalItem(title=goalTitleEditText.text.toString(),
            desc=goalDescEditText.text.toString(),
            image=null,
            timeStamp = System.currentTimeMillis(),
            priority = goalPrioSeekBar.progress)

        // Add the item to the start of the list
        goalList.add(0, newItem)

        // Save the goal list
        SaveUtil.writeGoalList(this, goalList)

        setResult(Activity.RESULT_OK)
        // Finally, close the activity as the goal has been added
        finish()
    }

    fun editGoalItem(@Suppress("UNUSED_PARAMETER") v: View) {
        val goalList = SaveUtil.loadGoalList(this)

        val editedItem = goalList[adapterPosition]
        editedItem.title = goalTitleEditText.text.toString()
        editedItem.desc = goalDescEditText.text.toString()
        editedItem.priority = goalPrioSeekBar.progress

        // No need to add the item again since we changed the reference inside the list

        SaveUtil.writeGoalList(this, goalList)

        val intent = Intent().apply {
            putExtra(ADAPTER_POS_BUNDLE_KEY, adapterPosition)
        }
        setResult(Activity.RESULT_OK, intent)

        finish()
    }

    fun cancelAddGoal(@Suppress("UNUSED_PARAMETER") v: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}