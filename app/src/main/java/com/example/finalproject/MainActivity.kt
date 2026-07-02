package com.example.finalproject

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject.adapter.GameAdapter
import com.example.finalproject.adapter.GameTouchHelper
import com.example.finalproject.data.GameDatabase
import com.example.finalproject.databinding.ActivityMainBinding
import com.example.finalproject.repository.GameRepository
import com.example.finalproject.ui.AddGameDialogFragment
import com.example.finalproject.viewmodel.GameViewModel
import com.example.finalproject.viewmodel.GameViewModelFactory
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: GameViewModel
    private lateinit var gameAdapter: GameAdapter
    private lateinit var touchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding ინიციალიზაცია — type-safe binding XML ელემენტებზე წვდომისთვის
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // ViewModel-ის ინიციალიზაცია Factory-ით — Repository Pattern
        val database = GameDatabase.getDatabase(applicationContext)
        val repository = GameRepository(database.gameDao())
        val factory = GameViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[GameViewModel::class.java]

        setupRecyclerView()

        // LiveData observation — UI ავტომატურად რეაგირებს მონაცემების ცვლილებაზე
        viewModel.filteredGames.observe(this) { games ->
            gameAdapter.submitList(games)
            binding.textViewEmpty.visibility = if (games.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewGames.visibility = if (games.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.fabAddGame.setOnClickListener {
            AddGameDialogFragment.newInstance().show(supportFragmentManager, "add_game")
        }
    }

    private fun setupRecyclerView() {
        gameAdapter = GameAdapter(
            onGameClick = { game ->
                AddGameDialogFragment.newInstance(game).show(supportFragmentManager, "edit_game")
            },
            onDragStart = { viewHolder ->
                touchHelper.startDrag(viewHolder)
            }
        )

        binding.recyclerViewGames.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = gameAdapter
        }

        // ItemTouchHelper — Drag & Drop რეალიზაცია RecyclerView-ში
        val touchCallback = GameTouchHelper(
            adapter = gameAdapter,
            onMoveComplete = { reorderedList ->
                viewModel.updateSortOrders(reorderedList)
            },
            onSwipedToDelete = { game ->
                viewModel.deleteGame(game)
                Snackbar.make(binding.root, "${game.title} წაიშალა", Snackbar.LENGTH_LONG)
                    .setAction("დაბრუნება") {
                        viewModel.insertGame(game)
                    }
                    .setActionTextColor(ContextCompat.getColor(this, R.color.retro_green))
                    .show()
            }
        )
        touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(binding.recyclerViewGames)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter_all -> { viewModel.setFilter(null); true }
            R.id.filter_backlog -> { viewModel.setFilter("Backlog"); true }
            R.id.filter_playing -> { viewModel.setFilter("Playing"); true }
            R.id.filter_finished -> { viewModel.setFilter("Finished"); true }
            R.id.sort_rating -> { viewModel.sortAllGamesByRating(); true }
            R.id.sort_title -> { viewModel.sortAllGamesByTitle(); true }
            R.id.action_random -> { showRandomGameDialog(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showRandomGameDialog() {
        viewModel.pickRandomBacklogGame { game ->
            val builder = AlertDialog.Builder(this)
            if (game != null) {
                builder.setTitle(R.string.random_game_title)
                    .setMessage("${game.title} (${game.platform})")
                    .setPositiveButton(R.string.play_now) { _, _ ->
                        viewModel.updateGame(game.copy(status = "Playing"))
                        Snackbar.make(binding.root, "${game.title} დაიწყეთ!", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(R.string.cancel, null)
            } else {
                builder.setMessage(R.string.random_game_empty)
                    .setPositiveButton(R.string.ok, null)
            }
            builder.show()
        }
    }
}
