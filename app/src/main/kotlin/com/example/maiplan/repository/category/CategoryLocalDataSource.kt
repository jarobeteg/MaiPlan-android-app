package com.example.maiplan.repository.category

import android.content.Context
import androidx.room.withTransaction
import com.example.maiplan.category.data.CategoryMutationPayload
import com.example.maiplan.category.data.CreateCategoryInput
import com.example.maiplan.category.data.UpdateCategoryInput
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.database.dao.CategoryDAO
import com.example.maiplan.database.dao.OutboxDAO
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.database.entities.OutboxEntity
import com.example.maiplan.network.sync.TideEntityType
import com.example.maiplan.network.sync.TideOperation
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleLocalResponse
import com.example.maiplan.repository.task.TaskCategoryLinks
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

class CategoryLocalDataSource(private val context: Context, private val databaseOverride: MaiPlanDatabase? = null) {
    companion object {
        private const val EMPTY_CATEGORY_NAME_ERROR = 1
    }

    private val database: MaiPlanDatabase by lazy {
        databaseOverride ?: MaiPlanDatabase.getDatabase(context)
    }

    private val categoryDao: CategoryDAO by lazy {
        database.categoryDAO()
    }

    private val outboxDao: OutboxDAO by lazy {
        database.outboxDAO()
    }

    private val gson: Gson by lazy {
        Gson()
    }

    fun observeCategories(userLocalId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.observeActiveCategories(userLocalId)
    }

    suspend fun getCategories(userLocalId: Long): Result<List<CategoryEntity>> {
        return handleLocalResponse { categoryDao.getActiveCategories(userLocalId) }
    }

    suspend fun getCategory(categoryLocalId: Long, userLocalId: Long): CategoryEntity? {
        return categoryDao.getCategoryByLocalId(categoryLocalId, userLocalId)
    }

    suspend fun createCategory(input: CreateCategoryInput, userLocalId: Long): Result<Unit> {
        val name = input.name.trim()
        val description = input.description.trim()

        validateCategory(name)?.let {
            return it
        }

        return handleLocalResponse {
            database.withTransaction {
                val now = Instant.now()
                val syncId = UUID.randomUUID()

                val category = CategoryEntity(
                    userLocalId = userLocalId,
                    name = name,
                    description = description,
                    color = input.color,
                    icon = input.icon,
                    syncId = syncId,
                    createdAt = now,
                    updatedAt = now
                )

                categoryDao.insertCategory(category)

                outboxDao.insertMutation(
                    OutboxEntity(
                        mutationId = UUID.randomUUID(),
                        userLocalId = userLocalId,
                        entityType = TideEntityType.CATEGORY,
                        entitySyncId = syncId,
                        operation = TideOperation.CREATE,
                        baseVersion = null,
                        payloadJson = category.toMutationPayloadJson(),
                        createdAt = now
                    )
                )

                Unit
            }
        }
    }

    suspend fun updateCategory(input: UpdateCategoryInput, userLocalId: Long): Result<Unit> {
        val name = input.name.trim()
        val description = input.description.trim()

        validateCategory(name)?.let {
            return it
        }

        return handleLocalResponse {
            database.withTransaction {
                val existing = categoryDao.getCategoryByLocalId(
                    categoryLocalId = input.categoryLocalId,
                    userLocalId = userLocalId
                ) ?: error("Category ${input.categoryLocalId} was not found")

                check(existing.deletedAt == null) {
                    "A deleted category cannot be updated"
                }

                val now = Instant.now()

                val updated = existing.copy(
                    name = name,
                    description = description,
                    color = input.color,
                    icon = input.icon,
                    updatedAt = now
                )

                check(categoryDao.updateCategory(updated) == 1) {
                    "Category update affected and unexpected number of rows"
                }

                outboxDao.insertMutation(
                    OutboxEntity(
                        mutationId = UUID.randomUUID(),
                        userLocalId = userLocalId,
                        entityType = TideEntityType.CATEGORY,
                        entitySyncId = existing.syncId,
                        operation = TideOperation.UPDATE,
                        baseVersion = existing.serverVersion,
                        payloadJson = updated.toMutationPayloadJson(),
                        createdAt = now
                    )
                )

                Unit
            }
        }
    }

    suspend fun softDeleteCategory(categoryLocalId: Long, userLocalId: Long): Result<Unit> {
        return handleLocalResponse {
            database.withTransaction {
                val existing = categoryDao.getCategoryByLocalId(
                    categoryLocalId = categoryLocalId,
                    userLocalId = userLocalId
                ) ?: error("Category $categoryLocalId was not found")

                if (existing.deletedAt == null) {
                    val now = Instant.now()

                    val tombstone = existing.copy(
                        updatedAt = now,
                        deletedAt = now
                    )

                    check(categoryDao.updateCategory(tombstone) == 1) {
                        "Category deletion affected an unexpected number of rows"
                    }

                    TaskCategoryLinks(database).categoryDeleted(userLocalId, existing.syncId)

                    outboxDao.insertMutation(
                        OutboxEntity(
                            mutationId = UUID.randomUUID(),
                            userLocalId = userLocalId,
                            entityType = TideEntityType.CATEGORY,
                            entitySyncId = existing.syncId,
                            operation = TideOperation.DELETE,
                            baseVersion = existing.serverVersion,
                            payloadJson = null,
                            createdAt = now
                        )
                    )
                }

                Unit
            }
        }
    }

    private fun validateCategory(name: String): Result.Failure? {
        return when {
            name.isBlank() -> Result.Failure(EMPTY_CATEGORY_NAME_ERROR)

            else -> null
        }
    }

    private fun CategoryEntity.toMutationPayloadJson(): String {
        return gson.toJson(
            CategoryMutationPayload(
                name = name,
                description = description,
                color = color,
                icon = icon
            )
        )
    }
}
