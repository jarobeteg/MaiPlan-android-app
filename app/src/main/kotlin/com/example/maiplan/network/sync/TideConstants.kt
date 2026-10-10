package com.example.maiplan.network.sync

object TideProtocol {
    const val VERSION = 3
    const val DEFAULT_DATA_LIMIT = 100
}

object TideClientConfig {
    const val UPLOAD_BATCH_SIZE = 100
    const val STALE_CLAIM_SECONDS = 15 * 60L
    const val MAX_PAGES_PER_RUN = 10
}

object TideEntityType {
    const val USER = "user"
    const val CATEGORY = "category"
    const val REMINDER = "reminder"
    const val EVENT = "event"
    const val NOTE = "note"
    const val TASK = "task"
    const val SUBTASK = "subtask"
    const val TASK_ACTION = "task_action"
    const val TASK_SERIES = "task_series"
    const val TASK_EXCLUSION = "task_exclusion"
    const val TASK_SERIES_ACTION = "task_series_action"

    val MUTABLE = listOf(CATEGORY, REMINDER, EVENT, NOTE, TASK_ACTION, TASK_SERIES_ACTION)
    val SUPPORTED = setOf(USER, CATEGORY, REMINDER, EVENT, NOTE, TASK, SUBTASK, TASK_SERIES, TASK_EXCLUSION)
}

object TideOperation {
    const val CREATE = "CREATE"
    const val UPDATE = "UPDATE"
    const val DELETE = "DELETE"
}

object TideRejectionCode {
    const val VERSION_CONFLICT = "VERSION_CONFLICT"
    const val MUTATION_DEPENDENCY_PENDING = "MUTATION_DEPENDENCY_PENDING"

    val RETRYABLE = setOf(MUTATION_DEPENDENCY_PENDING)
}
