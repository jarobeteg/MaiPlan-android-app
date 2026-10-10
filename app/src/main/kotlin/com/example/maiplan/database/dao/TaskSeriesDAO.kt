package com.example.maiplan.database.dao

import androidx.room.*
import com.example.maiplan.database.entities.*
import java.time.LocalDate
import java.util.UUID

@Dao
interface TaskSeriesDAO {
    @Insert suspend fun insert(row: TaskSeriesEntity): Long
    @Update suspend fun update(row: TaskSeriesEntity): Int
    @Query("SELECT * FROM task_series WHERE user_local_id = :user AND sync_id = :id")
    suspend fun get(user: Long, id: UUID): TaskSeriesEntity?
    @Query("SELECT * FROM task_series WHERE user_local_id = :user")
    suspend fun all(user: Long): List<TaskSeriesEntity>
    @Query("SELECT * FROM task_series WHERE user_local_id = :user ORDER BY sync_id")
    fun observeAll(user: Long): kotlinx.coroutines.flow.Flow<List<TaskSeriesEntity>>
    @Query("UPDATE task_series SET server_version = :version WHERE user_local_id = :user AND sync_id = :id AND (server_version IS NULL OR server_version < :version)")
    suspend fun version(user: Long, id: UUID, version: Long): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun exclude(row: TaskExclusionEntity)
    @Query("SELECT * FROM task_exclusion WHERE user_local_id = :user AND series_id = :series AND slot_date = :date")
    suspend fun exclusion(user: Long, series: UUID, date: LocalDate): TaskExclusionEntity?
}
