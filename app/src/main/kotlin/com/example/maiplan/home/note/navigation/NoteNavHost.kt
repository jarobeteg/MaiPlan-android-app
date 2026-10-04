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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.R
import com.example.maiplan.database.entities.ReminderEntity
import com.example.maiplan.home.note.screens.CreateNoteScreen
import com.example.maiplan.home.note.screens.NoteListScreen
import com.example.maiplan.home.note.screens.UpdateNoteScreen
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.utils.notifications.AlarmScheduler
import com.example.maiplan.utils.notifications.NotificationHelper
import com.example.maiplan.utils.toEpochMillis
import com.example.maiplan.viewmodel.note.NoteViewModel

@Composable
fun NoteNavHost(
    rootNavController: NavHostController,
    localNavController: NavHostController,
    noteViewModel: NoteViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var pendingNotificationPermissionAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var reminderAccessDialog by remember { mutableStateOf<String?>(null) }
    val continueWhenReady = {
        when {
            !NotificationHelper.canDeliverReminders(context) -> {
                reminderAccessDialog = "notifications"
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
                pendingNotificationPermissionAction != null && reminderAccessDialog != null) {
                continueWhenReady()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                continueWhenReady()
            } else {
                reminderAccessDialog = "notifications"
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
            pendingNotificationPermissionAction = onGranted
            continueWhenReady()
        }
    }

    reminderAccessDialog?.let { mode ->
        AlertDialog(
            onDismissRequest = {
                reminderAccessDialog = null
                pendingNotificationPermissionAction = null
            },
            title = { Text(stringResource(R.string.note_reminder_access_title)) },
            text = { Text(stringResource(
                if (mode == "exact") R.string.note_exact_alarm_access_message
                else R.string.note_notification_access_message,
            )) },
            confirmButton = {
                TextButton(onClick = {
                    if (mode == "exact") AlarmScheduler.requestExactAlarmPermission(context)
                    else NotificationHelper.openReminderNotificationSettings(context)
                }) { Text(stringResource(R.string.note_reminder_access_settings)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    reminderAccessDialog = null
                    pendingNotificationPermissionAction = null
                }) { Text(stringResource(android.R.string.cancel)) }
            },
        )
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
    val userLocalId = UserSession.userLocalId!!

    composable(NoteRoutes.NoteMain.route) {
        val context = LocalContext.current
        NoteListScreen(
            rootNavController = rootNavController,
            viewModel = noteViewModel,
            onCreateClick = { localNavController.navigate(NoteRoutes.Create.route) },
            onNoteClick = { note -> localNavController.navigate(NoteRoutes.Update.withArgs(note.noteLocalId)) },
            onDeleteClick = { note ->
                noteViewModel.softDeleteNote(note.noteLocalId, userLocalId)
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
                localNavController.popBackStack()
                noteViewModel.clearCreateResult()
            }
        )

        val result = noteViewModel.createNoteResult.observeAsState().value
        LaunchedEffect(result) {
            if (result is Result.Success) {
                result.data.reminderWarning?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                localNavController.popBackStack()
                noteViewModel.clearCreateResult()
            }
        }
    }

    composable(
        route = NoteRoutes.Update.route,
        arguments = listOf(navArgument("noteLocalId") { type = NavType.LongType })
    ) { backStackEntry ->
        val context = LocalContext.current
        val noteLocalId = backStackEntry.arguments?.getLong("noteLocalId") ?: return@composable
        val selectedNote = noteViewModel.getNote(noteLocalId) ?: return@composable
        val originalReminderLocalId = remember(noteLocalId) { selectedNote.reminderLocalId }

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
                localNavController.popBackStack()
                noteViewModel.clearUpdateResult()
            }
        )

        val result = noteViewModel.updateNoteResult.observeAsState().value
        LaunchedEffect(result) {
            if (result is Result.Success) {
                result.data.reminderWarning?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                localNavController.popBackStack()
                noteViewModel.clearUpdateResult()
            }
        }
    }
}
