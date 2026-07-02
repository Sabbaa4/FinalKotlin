package com.example.finalproject.repository

import com.example.finalproject.data.Game
import com.example.finalproject.data.GameDao
import kotlinx.coroutines.flow.Flow

// Repository — DAO-სთან წვდომის აბსტრაქციის ფენა (MVVM არქიტექტურა)
class GameRepository(private val gameDao: GameDao) {

    val allGames: Flow<List<Game>> = gameDao.getAllGamesSorted()

    fun getGamesByStatus(status: String): Flow<List<Game>> {
        return gameDao.getGamesByStatus(status)
    }

    suspend fun insert(game: Game): Long {
        return gameDao.insert(game)
    }

    suspend fun update(game: Game) {
        gameDao.update(game)
    }

    suspend fun delete(game: Game) {
        gameDao.delete(game)
    }

    suspend fun updateSortOrder(id: Int, sortOrder: Int) {
        gameDao.updateSortOrder(id, sortOrder)
    }

    suspend fun getMaxSortOrder(): Int? {
        return gameDao.getMaxSortOrder()
    }
}
