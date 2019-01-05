package com.example.kenneth.goals

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.app.Activity
import android.content.Intent
import android.widget.TextView
import org.w3c.dom.Text


class AddGoalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goal)

        val goalPrioritySlider = findViewById<SeekBar>(R.id.priority_slider)
        val priorityDisplay = findViewById<TextView>(R.id.priority_display)
        goalPrioritySlider.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                priorityDisplay.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    fun addNewGoalItem(@Suppress("UNUSED_PARAMETER") v: View) {
        val goalList = SaveUtil.loadGoalList(this)
        // Get relevant information from views
        val goalTitleField = findViewById<EditText>(R.id.title_edittext)
        val goalDescField = findViewById<EditText>(R.id.description_edittext)
        val goalPrioritySlider = findViewById<SeekBar>(R.id.priority_slider)

        val newItem = GoalItem(title=goalTitleField.text.toString(),
            desc=goalDescField.text.toString(),
            image=null,
            priority = goalPrioritySlider.progress)

        goalList.add(newItem)

        // Save the goal list
        SaveUtil.writeGoalList(this, goalList)

        setResult(Activity.RESULT_OK)
        // Finally, close the activity as the goal has been added
        finish()
    }

    fun cancelAddGoal(@Suppress("UNUSED_PARAMETER") v: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}