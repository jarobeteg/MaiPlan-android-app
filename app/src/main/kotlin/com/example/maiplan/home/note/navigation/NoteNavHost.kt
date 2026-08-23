package com.example.maiplan.home.note.navigation

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.home.note.screens.CreateNoteScreen
import com.example.maiplan.home.note.screens.NoteListScreen
import com.example.maiplan.home.note.screens.UpdateNoteScreen
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.utils.notifications.AlarmScheduler
import com.example.maiplan.utils.notifications.NotificationHelper
import com.example.maiplan.utils.notifications.ReminderData
import com.example.maiplan.utils.toEpochMillis
import com.example.maiplan.viewmodel.note.NoteViewModel

@Composable
fun NoteNavHost(
    rootNavController: NavHostController,
    localNavController: NavHostController,
    noteViewModel: NoteViewModel
) {
    val context = LocalContext.current
    var pendingNotificationPermissionAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            val pendingAction = pendingNotificationPermissionAction
            pendingNotificationPermissionAction = null
            if (granted) {
                pendingAction?.invoke()
            } else {
                Toast.makeText(
                    context,
                    "Allow notifications to save a note reminder.",
                    Toast.LENGTH_LONG,
                ).show()
            }
        },
    )
    val runWithNotificationPermission: (() -> Unit) -> Unit = { onGranted ->
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.canPostNotifications(context)
        ) {
            pendingNotificationPermissionAction = onGranted
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onGranted()
        }
    }

    NavHost(
        navController = localNavController,
        startDestination = NoteRoutes.NoteMain.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        noteNavGraph(
            localNavController = localNavController,
            rootNavController = rootNavController,
            noteViewModel = noteViewModel,
            runWithNotificationPermission = runWithNotificationPermission,
        )
    }
}

fun NavGraphBuilder.noteNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController,
    noteViewModel: NoteViewModel,
    runWithNotificationPermission: (() -> Unit) -> Unit,
) {
    val userId = UserSession.userId!!

    composable(NoteRoutes.NoteMain.route) {
        val context = LocalContext.current
        NoteListScreen(
            rootNavController = rootNavController,
            viewModel = noteViewModel,
            onCreateClick = { localNavController.navigate(NoteRoutes.Create.route) },
            onNoteClick = { note -> localNavController.navigate(NoteRoutes.Update.withArgs(note.noteId)) },
            onDeleteClick = { note ->
                note.reminderId?.let { AlarmScheduler.cancelAlarm(context, it) }
                noteViewModel.softDeleteNote(note.noteId, userId)
            }
        )
    }

    composable(NoteRoutes.Create.route) {
        val context = LocalContext.current
        CreateNoteScreen(
            viewModel = noteViewModel,
            onSaveClick = { title, content, category, reminderDateTime, reminderMessage ->
                val saveNote = {
                    val reminder = reminderDateTime?.let {
                        ReminderEntity(
                            userId = userId,
                            reminderTime = it.withSecond(0).withNano(0).toEpochMillis(),
                            message = reminderMessage,
                            syncState = 4,
                        )
                    }
                    noteViewModel.createNoteWithReminder(
                        reminder,
                        NoteEntity(
                            userId = userId,
                            categoryId = category?.categoryId,
                            title = title,
                            content = content,
                            syncState = 4
                        )
                    )
                }
                if (reminderDateTime != null) {
                    runWithNotificationPermission(saveNote)
                } else {
                    saveNote()
                }
            },
            onBackClick = {
                localNavController.popBackStack()
                noteViewModel.clearCreateResult()
            }
        )

        val result = noteViewModel.createNoteResult.observeAsState().value
        LaunchedEffect(result) {
            if (result is Result.Success) {
                val saved = result.data
                if (saved.reminderId != null && saved.reminderTime != null) {
                    val reminderData = ReminderData(
                        reminderId = saved.reminderId,
                        reminderTime = saved.reminderTime,
                        reminderTitle = saved.reminderTitle,
                        reminderMessage = saved.reminderMessage,
                    )
                    AlarmScheduler.attemptSchedule(context, reminderData)
                }
                localNavController.popBackStack()
                noteViewModel.clearCreateResult()
            }
        }
    }

    composable(
        route = NoteRoutes.Update.route,
        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
    ) { backStackEntry ->
        val context = LocalContext.current
        val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
        val selectedNote = noteViewModel.getNote(noteId) ?: return@composable
        val originalReminderId = remember(noteId) { selectedNote.reminderId }

        UpdateNoteScreen(
            viewModel = noteViewModel,
            note = selectedNote,
            onSaveClick = { title, content, category, reminderDateTime, reminderMessage ->
                val saveNote = {
                    val reminder = reminderDateTime?.let {
                        ReminderEntity(
                            reminderId = originalReminderId ?: 0,
                            userId = userId,
                            reminderTime = it.withSecond(0).withNano(0).toEpochMillis(),
                            message = reminderMessage,
                            syncState = if (originalReminderId == null) 4 else 2,
                        )
                    }
                    noteViewModel.updateNoteWithReminder(
                        reminder,
                        selectedNote.copy(
                            title = title,
                            content = content,
                            categoryId = category?.categoryId,
                            reminderId = originalReminderId,
                            syncState = 2
                        )
                    )
                }
                if (reminderDateTime != null) {
                    runWithNotificationPermission(saveNote)
                } else {
                    saveNote()
                }
            },
            onBackClick = {
                localNavController.popBackStack()
                noteViewModel.clearUpdateResult()
            }
        )

        val result = noteViewModel.updateNoteResult.observeAsState().value
        LaunchedEffect(result) {
            if (result is Result.Success) {
                val saved = result.data
                if (saved.reminderId != null && saved.reminderTime != null) {
                    val reminderData = ReminderData(
                        reminderId = saved.reminderId,
                        reminderTime = saved.reminderTime,
                        reminderTitle = saved.reminderTitle,
                        reminderMessage = saved.reminderMessage,
                    )
                    AlarmScheduler.attemptSchedule(context, reminderData)
                } else {
                    originalReminderId?.let { AlarmScheduler.cancelAlarm(context, it) }
                }
                localNavController.popBackStack()
                noteViewModel.clearUpdateResult()
            }
        }
    }
}
