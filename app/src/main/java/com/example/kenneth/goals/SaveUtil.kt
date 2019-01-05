package com.example.kenneth.goals

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SaveUtil {
    companion object {
        const val GOAL_LIST_KEY = "GOAL_LIST_SHARED_PREF"

        @JvmStatic
        fun loadGoalList(ctxt: Context): ArrayList<GoalItem> {
            // Set up necessary vars
            val sharedPref = ctxt.getSharedPreferences("shared pref", AppCompatActivity.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPref.getString(GOAL_LIST_KEY, null)
            val type = object: TypeToken<ArrayList<GoalItem>>() {}.type

            // Retrieve previously saved
            var goalList: ArrayList<GoalItem>? = gson.fromJson(json, type)

            if (goalList == null) {
                goalList = ArrayList<GoalItem>()
            }

            return goalList
        }

        @JvmStatic
        fun writeGoalList(ctxt: Context, goalList: ArrayList<GoalItem>) {
            // Set up necessary vars
            val sharedPref = ctxt.getSharedPreferences("shared pref", AppCompatActivity.MODE_PRIVATE)
            val gson = Gson()
            val editor = sharedPref.edit()

            // Save new array list under same key
            val savedJson = gson.toJson(goalList)
            editor.putString(GOAL_LIST_KEY, savedJson)
            editor.apply()
        }
    }
}