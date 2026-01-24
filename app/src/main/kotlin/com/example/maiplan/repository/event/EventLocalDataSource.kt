package com.example.maiplan.repository.event

import android.content.Context
import androidx.room.Transaction
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.EventDAO
import com.example.maiplan.database.dao.ReminderDAO
import com.example.maiplan.database.entities.EventEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse

class EventLocalDataSource(private val context: Context) {
    private val database: MaiPlanDatabase by lazy {
        MaiPlanDatabase.getDatabase(context)
    }

    private val reminderDao: ReminderDAO by lazy {
        database.reminderDAO()
    }

    private val eventDao: EventDAO by lazy {
        database.eventDAO()
    }

    @Transaction
    suspend fun createEventWithReminder(
        reminder: ReminderEntity?,
        event: EventEntity
    ) {
        val reminderId: Int? = reminder?.let {
            reminderDao.reminderInsert(it).toInt()
        }

        eventDao.eventInsert(event.copy(reminderId = reminderId))
    }

    @Transaction
    suspend fun updateEventWithReminder(
        reminder: ReminderEntity?,
        event: EventEntity
    ) {
        if (event.reminderId == null) {
            val reminderId: Int? = reminder?.let {
                reminderDao.reminderInsert(it).toInt()
            }

            eventDao.eventUpsert(event.copy(reminderId = reminderId))
        } else {
            reminderDao.reminderUpsert(reminder!!)
            eventDao.eventUpsert(event)
        }
    }

    suspend fun eventUpsert(event: EventEntity): Result<Unit> {
        return handleLocalResponse {
            eventDao.eventUpsert(event)
        }
    }

    suspend fun deleteEvent(event: EventEntity): Result<Unit> {
        return handleLocalResponse {
            eventDao.deleteEvent(event)
        }
    }

    suspend fun softDeleteEvent(eventId: Int, userId: Int): Result<Unit> {
        return handleLocalResponse {
            eventDao.softDeleteEvent(eventId, userId)
        }
    }

    suspend fun getPendingEvents(userId: Int): Result<List<EventEntity>> {
        return handleLocalResponse {
            eventDao.getPendingEvents(userId)
        }
    }

    suspend fun getEvent(eventId: Int): EventEntity {
        return eventDao.getEvent(eventId)
    }

    suspend fun getEventForRange(startMillis: Long, endMillis: Long, userId: Int): List<EventEntity> {
        return eventDao.getEventsForRange(startMillis, endMillis, userId)
    }
}