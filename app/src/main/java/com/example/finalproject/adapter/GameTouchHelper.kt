package com.example.finalproject.adapter

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.data.Game

// ItemTouchHelper — RecyclerView-ში Drag & Drop და Swipe ჟესტების მართვა
class GameTouchHelper(
    private val adapter: GameAdapter,
    private val onMoveComplete: (List<Game>) -> Unit,
    private val onSwipedToDelete: (Game) -> Unit
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.LEFT
) {

    // ელემენტის გადაადგილებისას adapter-ში პოზიციები იცვლება
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.moveItem(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    // მარცხნივ გადაფურცვლა თამაშის წაშლას იწვევს
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val game = adapter.getGameAt(viewHolder.bindingAdapterPosition)
        onSwipedToDelete(game)
    }

    // გადათრევის დასრულებისას ახალი თანმიმდევრობა ინახება ბაზაში
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        val reorderedList = (0 until adapter.itemCount).map { adapter.getGameAt(it) }
        onMoveComplete(reorderedList)
    }
}
