package com.example.maiplan.utils.notifications

import android.content.Intent
import android.net.Uri
import com.example.maiplan.database.MaiPlanDatabase
import java.util.UUID

data class TaskReminderDestination(val taskSyncId: UUID, val ownerSyncId: UUID) {
    fun uri(): Uri = Uri.Builder().scheme("maiplan").authority("task-reminder")
        .appendPath(ownerSyncId.toString()).appendPath(taskSyncId.toString()).build()

    companion object {
        fun from(intent: Intent?): TaskReminderDestination? = from(intent?.data)
        fun from(uri: Uri?): TaskReminderDestination? = runCatching {
            if (uri?.scheme != "maiplan" || uri.authority != "task-reminder" || uri.pathSegments.size != 2) return null
            TaskReminderDestination(UUID.fromString(uri.pathSegments[1]), UUID.fromString(uri.pathSegments[0]))
        }.getOrNull()
    }
}

suspend fun resolveTaskReminderDestination(db: MaiPlanDatabase, destination: TaskReminderDestination,
    signedInOwner: UUID?): Long? {
    if (signedInOwner != destination.ownerSyncId) return null
    val user = db.userDAO().getActiveUserBySyncId(destination.ownerSyncId) ?: return null
    val task = db.taskDAO().getTaskBySyncId(destination.taskSyncId, user.userLocalId)?.takeIf { it.deletedAt == null } ?: return null
    task.seriesId?.let {
        val series = db.taskSeriesDAO().get(user.userLocalId, UUID.fromString(it)) ?: return null
        if (series.deletedAt != null) return null
    }
    return task.taskLocalId
}
