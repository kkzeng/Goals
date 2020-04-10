package com.example.kenneth.goals

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DATABASE_NAME = "GoalItems"
const val TABLE_NAME = "TaskTable"

class DatabaseHelper(val context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTableStr: String = "CREATE TABLE $TABLE_NAME (Title INTEGER, Description TEXT, Priority INTEGER, Timestamp LONG)"
        db?.execSQL(createTableStr)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableStr: String = "DROP IF TABLE EXISTS $TABLE_NAME"
        db?.execSQL(dropTableStr)
    }

    fun addGoalItem(goalItem: GoalItem) {
        val db: SQLiteDatabase = writableDatabase
        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put("Title", goalItem.title)
            put("Description", goalItem.desc)
            put("Priority", goalItem.priority)
            put("Timestamp", goalItem.timeStamp)
        }

        db.insert(TABLE_NAME, null, values)
    }

    fun writeAllGoalItems(goalList: List<GoalItem>) {
        
    }

    fun retrieveAllGoalItems(): List<GoalItem> {
        val db: SQLiteDatabase = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        cursor.moveToFirst()
        val goalList: MutableList<GoalItem> = mutableListOf()
        while(!cursor.isAfterLast) {
        // Reconstruct goal item
            val title = cursor.getString(0)
            val desc = cursor.getString(1)
            val priority = cursor.getInt(2)
            val timestamp = cursor.getLong(3)
            val goalItem = GoalItem(title, desc, priority, timestamp, null)
            goalList.add(goalItem)
        }
        cursor.close()
        return goalList
    }
}