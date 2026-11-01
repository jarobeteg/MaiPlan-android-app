package com.example.maiplan.home.note.navigation

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.R
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.home.note.screens.CreateNoteScreen
import com.example.maiplan.home.note.screens.NoteListScreen
import com.example.maiplan.home.note.screens.NotePrimary
import com.example.maiplan.home.note.screens.NoteInk
import com.example.maiplan.home.note.screens.NoteMuted
import com.example.maiplan.home.note.screens.UpdateNoteScreen
import com.example.maiplan.home.note.screens.NoteLoadScreen
import com.example.maiplan.home.note.NoteScreenManager
import com.example.maiplan.repository.Result
import com.example.maiplan.theme.LocalAppDarkTheme
import com.example.maiplan.utils.notifications.AlarmScheduler
import com.example.maiplan.utils.notifications.NotificationHelper
import com.example.maiplan.utils.toEpochMillis

private typealias NoteReminderAction = (() -> Unit) -> Unit

@Composable
private fun NoteReminderAccess(content: @Composable (NoteReminderAction) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var pendingNotificationPermissionAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var reminderAccessDialog by remember { mutableStateOf<String?>(null) }
    val continueWhenReady = {
        when {
            !NotificationHelper.canDeliverReminders(context) -> {
                reminderAccessDialog = "settings"
            }
            !AlarmScheduler.canScheduleExactAlarms(context) -> {
                reminderAccessDialog = "exact"
            }
            else -> {
                reminderAccessDialog = null
                val action = pendingNotificationPermissionAction
                pendingNotificationPermissionAction = null
                action?.invoke()
            }
        }
    }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                pendingNotificationPermissionAction != null && reminderAccessDialog == null) {
                continueWhenReady()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (pendingNotificationPermissionAction != null) {
                if (granted) {
                    continueWhenReady()
                } else {
                    reminderAccessDialog = "settings"
                }
            }
        },
    )
    val runWithNotificationPermission: (() -> Unit) -> Unit = { onGranted ->
        pendingNotificationPermissionAction = onGranted
        if (!NotificationHelper.canDeliverReminders(context)) {
            reminderAccessDialog = if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !NotificationHelper.canPostNotifications(context)
            ) "permission" else "settings"
        } else {
            continueWhenReady()
        }
    }

    reminderAccessDialog?.let { mode ->
        val dark = LocalAppDarkTheme.current
        AlertDialog(
            onDismissRequest = {
                reminderAccessDialog = null
                pendingNotificationPermissionAction = null
            },
            title = { Text(stringResource(
                when (mode) {
                    "permission" -> R.string.note_notifications_permission_title
                    "exact" -> R.string.event_exact_alarm_title
                    else -> R.string.note_reminder_access_title
                },
            )) },
            text = { Text(stringResource(
                when (mode) {
                    "permission" -> R.string.note_notifications_permission_message
                    "exact" -> R.string.note_exact_alarm_access_message
                    else -> R.string.note_notification_access_message
                },
            )) },
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = NotePrimary),
                    onClick = {
                        reminderAccessDialog = null
                        when (mode) {
                            "permission" -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            "exact" -> AlarmScheduler.requestExactAlarmPermission(context)
                            else -> NotificationHelper.openReminderNotificationSettings(context)
                        }
                    },
                ) {
                    Text(stringResource(
                        when (mode) {
                            "permission" -> R.string.event_notifications_allow
                            "exact" -> R.string.event_exact_alarm_open_settings
                            else -> R.string.note_reminder_access_settings
                        },
                    ))
                }
            },
            dismissButton = {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = NotePrimary),
                    onClick = {
                        reminderAccessDialog = null
                        pendingNotificationPermissionAction = null
                    },
                ) { Text(stringResource(R.string.note_reminder_back_to_editor)) }
            },
            containerColor = if (dark) Color(0xFF191D2E) else Color.White,
            titleContentColor = if (dark) Color(0xFFF5F7FB) else NoteInk,
            textContentColor = if (dark) Color(0xFFAEB7C9) else NoteMuted,
        )
    }

    content(runWithNotificationPermission)
}

fun NavGraphBuilder.noteNavGraph(navController: NavHostController) {
    composable(NoteRoutes.NoteMain.route) { entry ->
        NoteScreenManager(navController, entry) { userLocalId, noteViewModel ->
            LaunchedEffect(userLocalId) { noteViewModel.loadNotes(userLocalId) }
            NoteListScreen(
                rootNavController = navController,
                viewModel = noteViewModel,
                onCreateClick = { navController.navigate(NoteRoutes.Create.route) },
                onNoteClick = { note -> navController.navigate(NoteRoutes.Update.withArgs(note.noteLocalId)) },
                onPinClick = { note -> noteViewModel.setNotePinned(note, !note.isPinned) },
                onDeleteClick = { note ->
                    noteViewModel.softDeleteNote(note.noteLocalId, userLocalId)
                }
            )
        }
    }

    composable(NoteRoutes.Create.route) { entry ->
        NoteScreenManager(navController, entry) { userLocalId, noteViewModel ->
            NoteReminderAccess { runWithNotificationPermission ->
                val context = LocalContext.current
                CreateNoteScreen(
                    viewModel = noteViewModel,
                    onSaveClick = { title, content, category, reminderDateTime, reminderMessage ->
                        val saveNote = {
                            val reminder = reminderDateTime?.let {
                                ReminderEntity(
                                    userLocalId = userLocalId,
                                    reminderTime = it.withSecond(0).withNano(0).toEpochMillis(),
                                    message = reminderMessage,
                                )
                            }
                            noteViewModel.createNoteWithReminder(
                                reminder,
                                NoteEntity(
                                    userLocalId = userLocalId,
                                    categoryLocalId = category?.categoryLocalId,
                                    title = title,
                                    content = content,
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
                        navController.popBackStack()
                        noteViewModel.clearCreateResult()
                    }
                )

                val result = noteViewModel.createNoteResult.observeAsState().value
                LaunchedEffect(result) {
                    if (result is Result.Success) {
                        result.data.reminderWarning?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        navController.popBackStack()
                        noteViewModel.clearCreateResult()
                    }
                }
            }
        }
    }

    composable(
        route = NoteRoutes.Update.route,
        arguments = listOf(navArgument("noteLocalId") { type = NavType.LongType })
    ) { backStackEntry ->
        val noteLocalId = backStackEntry.arguments?.getLong("noteLocalId") ?: return@composable
        NoteScreenManager(navController, backStackEntry) { userLocalId, noteViewModel ->
            val context = LocalContext.current
            var selected by remember(noteLocalId, userLocalId) { mutableStateOf<NoteEntity?>(null) }
            var loading by remember(noteLocalId, userLocalId) { mutableStateOf(true) }
            var loadAttempt by remember(noteLocalId, userLocalId) { mutableStateOf(0) }
            val categories by noteViewModel.categoryList.observeAsState()
            LaunchedEffect(noteLocalId, userLocalId, loadAttempt) {
                loading = true
                selected = (noteViewModel.getNoteForEdit(noteLocalId, userLocalId) as? Result.Success)?.data
                loading = false
            }
            val selectedNote = selected
            if (selectedNote == null || categories == null) {
                NoteLoadScreen(loading || categories == null, { navController.popBackStack() }, { loadAttempt++ })
                return@NoteScreenManager
            }
            val originalReminderLocalId = remember(noteLocalId) { selectedNote.reminderLocalId }

            NoteReminderAccess { runWithNotificationPermission ->
                UpdateNoteScreen(
                    viewModel = noteViewModel,
                    note = selectedNote,
                    onSaveClick = { title, content, category, reminderDateTime, reminderMessage ->
                        val saveNote = {
                            val reminder = reminderDateTime?.let {
                                ReminderEntity(
                                    reminderLocalId = originalReminderLocalId ?: 0L,
                                    userLocalId = userLocalId,
                                    reminderTime = it.withSecond(0).withNano(0).toEpochMillis(),
                                    message = reminderMessage,
                                )
                            }
                            noteViewModel.updateNoteWithReminder(
                                reminder,
                                selectedNote.copy(
                                    title = title,
                                    content = content,
                                    categoryLocalId = category?.categoryLocalId,
                                    reminderLocalId = originalReminderLocalId,
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
                        navController.popBackStack()
                        noteViewModel.clearUpdateResult()
                    }
                )

                val result = noteViewModel.updateNoteResult.observeAsState().value
                LaunchedEffect(result) {
                    if (result is Result.Success) {
                        result.data.reminderWarning?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        navController.popBackStack()
                        noteViewModel.clearUpdateResult()
                    }
                }
            }
        }
    }
}
