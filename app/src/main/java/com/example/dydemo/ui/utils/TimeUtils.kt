package com.example.dydemo.ui.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    fun formatMessageTimestamp(timestamp: Long): String {
        val now = Calendar.getInstance()
        val messageTime = Calendar.getInstance().apply { timeInMillis = timestamp }

        val diffMillis = now.timeInMillis - messageTime.timeInMillis
        val diffMinutes = diffMillis / (1000 * 60)
        val diffHours = diffMillis / (1000 * 60 * 60)

        return when {
            diffMinutes < 1 -> "刚刚"
            diffHours < 1 -> "${diffMinutes}分钟前"
            isSameDay(now, messageTime) -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            isYesterday(now, messageTime) -> "昨天 " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            isWithinAWeek(now, messageTime) -> "${getDaysDifference(now, messageTime)}天前"
            else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, messageTime: Calendar): Boolean {
        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        return isSameDay(yesterday, messageTime)
    }

    private fun isWithinAWeek(now: Calendar, messageTime: Calendar): Boolean {
        val diffMillis = now.timeInMillis - messageTime.timeInMillis
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)
        return diffDays < 7
    }
    
    private fun getDaysDifference(now: Calendar, messageTime: Calendar): Int {
        val diffMillis = now.timeInMillis - messageTime.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}
