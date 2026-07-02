package com.example.finalproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.Game
import com.example.finalproject.repository.GameRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private val _currentFilter = MutableLiveData<String?>(null)

    // switchMap რეაქტიულად ცვლის მონაცემთა წყაროს ფილტრის მიხედვით
    val filteredGames: LiveData<List<Game>> = _currentFilter.switchMap { filter ->
        if (filter == null || filter == "ყველა") {
            repository.allGames.asLiveData()
        } else {
            repository.getGamesByStatus(filter).asLiveData()
        }
    }

    fun setFilter(status: String?) {
        _currentFilter.value = status
    }

    // viewModelScope — კორუტინა ავტომატურად გაუქმდება ViewModel-ის განადგურებისას
    fun insertGame(game: Game) = viewModelScope.launch {
        val maxOrder = repository.getMaxSortOrder() ?: 0
        val gameWithOrder = game.copy(sortOrder = maxOrder + 1)
        repository.insert(gameWithOrder)
    }

    fun updateGame(game: Game) = viewModelScope.launch {
        repository.update(game)
    }

    fun deleteGame(game: Game) = viewModelScope.launch {
        repository.delete(game)
    }

    // Drag & Drop-ის შემდეგ ყველა ელემენტის sortOrder განახლდება
    fun updateSortOrders(games: List<Game>) = viewModelScope.launch {
        games.forEachIndexed { index, game ->
            repository.updateSortOrder(game.id, index)
        }
    }

    fun sortAllGamesByTitle() = viewModelScope.launch {
        val currentList = repository.allGames.first()
        val sorted = currentList.sortedBy { it.title.lowercase() }
        updateSortOrders(sorted)
    }

    fun sortAllGamesByRating() = viewModelScope.launch {
        val currentList = repository.allGames.first()
        val sorted = currentList.sortedByDescending { it.rating }
        updateSortOrders(sorted)
    }

    fun pickRandomBacklogGame(onResult: (Game?) -> Unit) = viewModelScope.launch {
        val currentList = repository.allGames.first()
        val backlogGames = currentList.filter { it.status == "Backlog" }
        onResult(if (backlogGames.isNotEmpty()) backlogGames.random() else null)
    }
}

// Factory — ViewModel-ის შესაქმნელად Repository-ის გადაცემით
class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
