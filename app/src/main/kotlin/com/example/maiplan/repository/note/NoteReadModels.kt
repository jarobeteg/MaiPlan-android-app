package com.example.maiplan.repository.note

import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.NoteEntity

data class NoteReminderItem(val note: NoteEntity, val reminderTime: Long, val category: CategoryEntity?)
