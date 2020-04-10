package com.example.kenneth.goals

import android.support.annotation.DrawableRes

data class GoalItem(var title: String,
                    var desc: String,
                    var priority: Int,
                    val timeStamp: Long, // Follows system time
                    @DrawableRes var image: Int?)

// TODO: Maybe add completion progression slider later on in CardView