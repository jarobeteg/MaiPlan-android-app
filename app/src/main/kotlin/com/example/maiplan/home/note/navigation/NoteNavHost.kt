package com.example.maiplan.home.note.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.maiplan.database.entities.NoteEntity
import com.example.maiplan.home.note.screens.CreateNoteScreen
import com.example.maiplan.home.note.screens.NoteListScreen
import com.example.maiplan.home.note.screens.UpdateNoteScreen
import com.example.maiplan.repository.Result
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.viewmodel.note.NoteViewModel

@Composable
fun NoteNavHost(
    rootNavController: NavHostController,
    localNavController: NavHostController,
    noteViewModel: NoteViewModel
) {
    NavHost(
        navController = localNavController,
        startDestination = NoteRoutes.NoteMain.route,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        noteNavGraph(localNavController, rootNavController, noteViewModel)
    }
}

fun NavGraphBuilder.noteNavGraph(
    localNavController: NavController,
    rootNavController: NavHostController,
    noteViewModel: NoteViewModel
) {
    val userId = UserSession.userId!!

    composable(NoteRoutes.NoteMain.route) {
        NoteListScreen(
            rootNavController = rootNavController,
            viewModel = noteViewModel,
            onCreateClick = { localNavController.navigate(NoteRoutes.Create.route) },
            onNoteClick = { note -> localNavController.navigate(NoteRoutes.Update.withArgs(note.noteId)) },
            onDeleteClick = { noteId -> noteViewModel.softDeleteNote(noteId, userId) }
        )
    }

    composable(NoteRoutes.Create.route) {
        CreateNoteScreen(
            viewModel = noteViewModel,
            onSaveClick = { title, content, category ->
                noteViewModel.createNote(
                    NoteEntity(
                        userId = userId,
                        categoryId = category?.categoryId,
                        title = title,
                        content = content,
                        syncState = 4
                    )
                )
            },
            onBackClick = {
                localNavController.popBackStack()
                noteViewModel.clearCreateResult()
            }
        )

        val result = noteViewModel.createNoteResult.observeAsState().value
        LaunchedEffect(result) {
            if (result is Result.Success) {
                localNavController.popBackStack()
                noteViewModel.clearCreateResult()
            }
        }
    }

    composable(
        route = NoteRoutes.Update.route,
        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
    ) { backStackEntry ->
        val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
        val selectedNote = noteViewModel.getNote(noteId) ?: return@composable

        UpdateNoteScreen(
            viewModel = noteViewModel,
            note = selectedNote,
            onSaveClick = { title, content, category ->
                noteViewModel.updateNote(
                    selectedNote.copy(
                        title = title,
                        content = content,
                        categoryId = category?.categoryId,
                        syncState = 2
                    )
                )
            },
            onBackClick = {
                localNavController.popBackStack()
                noteViewModel.clearUpdateResult()
            }
        )

        val result = noteViewModel.updateNoteResult.observeAsState().value
        LaunchedEffect(result) {
            if (result is Result.Success) {
                localNavController.popBackStack()
                noteViewModel.clearUpdateResult()
            }
        }
    }
}
