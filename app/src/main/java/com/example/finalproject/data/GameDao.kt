package com.example.finalproject.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// DAO ინტერფეისი — მონაცემთა ბაზასთან წვდომის მეთოდები
@Dao
interface GameDao {

    // Flow რეაქტიულად აბრუნებს მონაცემებს sortOrder-ის მიხედვით
    @Query("SELECT * FROM games ORDER BY sortOrder ASC")
    fun getAllGamesSorted(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE status = :status ORDER BY sortOrder ASC")
    fun getGamesByStatus(status: String): Flow<List<Game>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: Game): Long

    @Update
    suspend fun update(game: Game)

    @Delete
    suspend fun delete(game: Game)

    @Query("UPDATE games SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Int, sortOrder: Int)

    // ახალი თამაშის დამატებისას მაქსიმალური sortOrder-ის მისაღებად
    @Query("SELECT MAX(sortOrder) FROM games")
    suspend fun getMaxSortOrder(): Int?
}
