package com.example.maiplan.utils.notifications

data class ReminderData(
    val reminderLocalId: Long,
    val reminderTime: Long,
    val reminderTitle: String,
    val reminderMessage: String
)
