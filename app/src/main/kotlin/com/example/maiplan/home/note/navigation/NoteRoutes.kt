package com.example.maiplan.home.note.navigation

sealed class NoteRoutes(val route: String) {
    data object NoteMain : NoteRoutes("note-main-screen")
    data object Create : NoteRoutes("create-note")
    data object Update : NoteRoutes("update-note/{noteId}") {
        fun withArgs(noteId: Int) = "update-note/$noteId"
    }
}
