package com.example.maiplan.viewmodel.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.home.event.utils.CalendarEventUI
import com.example.maiplan.repository.category.CategoryLocalDataSource
import com.example.maiplan.repository.event.EventLocalDataSource
import com.example.maiplan.repository.event.EventRepository
import com.example.maiplan.repository.reminder.ReminderLocalDataSource
import com.example.maiplan.repository.note.NoteReminderItem
import com.example.maiplan.repository.note.NoteRepository
import com.example.maiplan.repository.note.NoteLocalDataSource
import com.example.maiplan.repository.task.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId

private data class HomeDay(val date: LocalDate, val zone: ZoneId)

data class HomeSection<T>(
    val items: List<T> = emptyList(),
    val loading: Boolean = true,
    val failed: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val observeEvents: (LocalDate) -> Flow<List<CalendarEventUI>>,
    private val observeTasks: (LocalDate) -> Flow<List<TaskSummary>>,
    private val observeNotes: (LocalDate, ZoneId) -> Flow<List<NoteReminderItem>>,
) : ViewModel() {
    private val day = MutableStateFlow(HomeDay(LocalDate.now(), ZoneId.systemDefault()))
    private val retry = MutableStateFlow(0)

    private fun <T> section(observe: (HomeDay) -> Flow<List<T>>) =
        combine(day, retry) { date, _ -> date }.flatMapLatest { date ->
            observe(date).map { HomeSection(items = it, loading = false) }
                .onStart { emit(HomeSection()) }
                .catch { emit(HomeSection(loading = false, failed = true)) }
        }.flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeSection())

    val events = section { observeEvents(it.date) }
    val tasks = section { observeTasks(it.date) }
    val notes = section { observeNotes(it.date, it.zone) }

    fun showDay(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()) { day.value = HomeDay(date, zone) }
    fun retry() { retry.value++ }

    companion object {
        fun create(context: Context, userId: Long): HomeViewModel {
            val app = context.applicationContext
            val database = MaiPlanDatabase.getDatabase(app)
            val events = EventRepository(app, EventLocalDataSource(app),
                CategoryLocalDataSource(app), ReminderLocalDataSource(app))
            val tasks = TaskRepository.create(app)
            val notes = NoteRepository(app, NoteLocalDataSource(app),
                CategoryLocalDataSource(app), ReminderLocalDataSource(app))
            return HomeViewModel(
                observeEvents = { date ->
                    database.invalidationTracker.createFlow("event", "category", "reminder")
                        .map { events.getEventsForMonth(date, date.plusDays(1), userId)[date].orEmpty() }
                },
                observeTasks = { date ->
                    combine(tasks.observeTasks(userId, TaskQuery(date = TaskDateFilter.On(date),
                        statuses = TaskStatus.entries.toSet())),
                        database.categoryDAO().observeActiveCategories(userId)) { rows, categories ->
                        val presentations = categories.associate { category ->
                            category.categoryLocalId to TaskCategoryPresentation(category.categoryLocalId,
                                category.syncId, category.name, category.color, category.icon)
                        }
                        rows.map { it.summary(presentations[it.effectiveCategoryLocalId]) }
                    }
                },
                observeNotes = { date, zone -> notes.observeNotesForDay(userId, date, zone) },
            )
        }
    }
}
