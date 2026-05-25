package com.example.maiplan.utils.notifications

data class ReminderData(
    val reminderId: Int,
    val reminderTime: Long,
    val reminderTitle: String,
    val reminderMessage: String
)