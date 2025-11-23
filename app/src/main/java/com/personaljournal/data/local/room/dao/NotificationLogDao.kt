package com.personaljournal.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.personaljournal.data.local.room.entity.NotificationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun observeLogs(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: NotificationLogEntity)

    @Query("DELETE FROM notification_logs")
    suspend fun clearAll()

    @Query(
        """
        DELETE FROM notification_logs
        WHERE id NOT IN (
            SELECT id FROM notification_logs
            ORDER BY timestamp DESC
            LIMIT :maxItems
        )
        """
    )
    suspend fun trimTo(maxItems: Int)
}
