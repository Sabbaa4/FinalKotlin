package com.example.finalproject.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.Game
import com.example.finalproject.databinding.ItemGameBinding

class GameAdapter(
    private val onGameClick: (Game) -> Unit,
    private val onDragStart: (RecyclerView.ViewHolder) -> Unit
) : ListAdapter<Game, GameAdapter.GameViewHolder>(GameDiffCallback()) {

    // ViewBinding — type-safe binding XML ელემენტებზე წვდომისთვის
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GameViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = getItem(position)
        holder.bind(game)

        holder.itemView.setOnClickListener {
            onGameClick(game)
        }

        // Drag handle-ზე შეხებისას იწყება გადათრევის პროცესი
        holder.binding.imageViewDragHandle.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                onDragStart(holder)
            }
            false
        }
    }

    fun moveItem(from: Int, to: Int) {
        val list = currentList.toMutableList()
        val item = list.removeAt(from)
        list.add(to, item)
        submitList(list)
    }

    fun getGameAt(position: Int): Game = getItem(position)

    class GameViewHolder(
        val binding: ItemGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: Game) {
            binding.textViewTitle.text = game.title
            binding.textViewPlatform.text = game.platform

            binding.textViewStatus.text = game.status
            val context = binding.root.context
            val statusColor = when (game.status) {
                "Backlog" -> ContextCompat.getColor(context, R.color.retro_yellow)
                "Playing" -> ContextCompat.getColor(context, R.color.retro_cyan)
                "Finished" -> ContextCompat.getColor(context, R.color.retro_green)
                else -> ContextCompat.getColor(context, R.color.retro_text_secondary)
            }
            binding.textViewStatus.setTextColor(statusColor)

            val isFinished = game.status == "Finished"
            binding.ratingContainer.visibility = if (isFinished) android.view.View.VISIBLE else android.view.View.GONE

            // რეიტინგის მიხედვით ვარსკვლავები ივსება ან ცარიელდება
            val stars = listOf(
                binding.star1, binding.star2, binding.star3,
                binding.star4, binding.star5
            )
            stars.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index < game.rating) R.drawable.ic_star_filled
                    else R.drawable.ic_star_empty
                )
            }
        }
    }

    // DiffUtil ეფექტურად ადარებს ძველ და ახალ სიას, მხოლოდ შეცვლილი ელემენტები განახლდება
    class GameDiffCallback : DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem == newItem
        }
    }
}
