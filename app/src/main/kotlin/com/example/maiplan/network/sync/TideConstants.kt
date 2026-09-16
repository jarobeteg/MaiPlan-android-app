package com.example.maiplan.network.sync

object TideProtocol {
    const val VERSION = 1
    const val DEFAULT_DATA_LIMIT = 100
}

object TideClientConfig {
    const val UPLOAD_BATCH_SIZE = 100
    const val STALE_CLAIM_SECONDS = 15 * 60L
    const val MAX_PAGES_PER_RUN = 10
}

object TideEntityType {
    const val CATEGORY = "category"
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
