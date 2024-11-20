package com.arcmce.boogaloo.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Extension function for formatting date
fun ZonedDateTime.toDayWithSuffix(): String {
    val dayOfWeek = this.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val dayOfMonth = this.dayOfMonth
    val suffix = getDaySuffix(dayOfMonth)

    return "$dayOfWeek $dayOfMonth$suffix"
}

// Helper function to get suffix for the day
private fun getDaySuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}

fun ZonedDateTime.toTimeFormat(): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    return this.format(formatter)
}
