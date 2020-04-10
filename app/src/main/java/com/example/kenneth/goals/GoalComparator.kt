package com.example.kenneth.goals

import java.util.Comparator

private const val PRIORITY_WEIGHT = 0.7
private const val SENIORITY_WEIGHT = 0.3

private const val THREE_DAYS_MS = 259200000L
private const val ONE_WEEK_MS = 604800000L
private const val ONE_MONTH_MS = 2592000000L

class GoalItemComparator : Comparator<GoalItem> {
    override fun compare(c1: GoalItem, c2: GoalItem): Int {

        // Sorts in descending order of priority
        val prioDiff = c2.priority.compareTo(c1.priority)

        // We want smaller timeStamps first since they are older
        return if(prioDiff == 0) c1.timeStamp.compareTo(c2.timeStamp)
        else prioDiff
    }
}