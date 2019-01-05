package com.example.kenneth.goals

import android.support.annotation.DrawableRes

data class GoalItem(var title: String,
                    var desc: String,
                    var priority: Int,
                    @DrawableRes var image: Int?)

// Maybe add completion progression slider later on in CardView