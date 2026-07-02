package com.example.finalproject.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.finalproject.R
import com.example.finalproject.data.Game
import com.example.finalproject.data.GameDatabase
import com.example.finalproject.databinding.DialogAddGameBinding
import com.example.finalproject.repository.GameRepository
import com.example.finalproject.viewmodel.GameViewModel
import com.example.finalproject.viewmodel.GameViewModelFactory

class AddGameDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_GAME_ID = "game_id"
        private const val ARG_GAME_TITLE = "game_title"
        private const val ARG_GAME_PLATFORM = "game_platform"
        private const val ARG_GAME_STATUS = "game_status"
        private const val ARG_GAME_RATING = "game_rating"
        private const val ARG_GAME_SORT_ORDER = "game_sort_order"

        fun newInstance(game: Game? = null): AddGameDialogFragment {
            val fragment = AddGameDialogFragment()
            if (game != null) {
                val args = Bundle().apply {
                    putInt(ARG_GAME_ID, game.id)
                    putString(ARG_GAME_TITLE, game.title)
                    putString(ARG_GAME_PLATFORM, game.platform)
                    putString(ARG_GAME_STATUS, game.status)
                    putInt(ARG_GAME_RATING, game.rating)
                    putInt(ARG_GAME_SORT_ORDER, game.sortOrder)
                }
                fragment.arguments = args
            }
            return fragment
        }
    }

    private var _binding: DialogAddGameBinding? = null
    private val binding get() = _binding!!
    private var currentRating = 3
    private lateinit var viewModel: GameViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // ViewBinding ინიციალიზაცია — DialogFragment-ში inflate ხდება ხელით
        _binding = DialogAddGameBinding.inflate(layoutInflater)

        // ViewModel-ის ინიციალიზაცია
        val database = GameDatabase.getDatabase(requireContext())
        val repository = GameRepository(database.gameDao())
        val factory = GameViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[GameViewModel::class.java]

        // Spinner-ის კონფიგურაცია
        val statuses = listOf("Backlog", "Playing", "Finished")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statuses
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = spinnerAdapter

        binding.spinnerStatus.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedStatus = statuses[position]
                val isFinished = selectedStatus == "Finished"
                
                binding.ratingLabel.visibility = if (isFinished) android.view.View.VISIBLE else android.view.View.GONE
                binding.ratingContainer.visibility = if (isFinished) android.view.View.VISIBLE else android.view.View.GONE
                
                if (!isFinished) {
                    currentRating = 0
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        // ვარსკვლავების click listener-ების კონფიგურაცია
        val stars = listOf(
            binding.ratingStar1, binding.ratingStar2, binding.ratingStar3,
            binding.ratingStar4, binding.ratingStar5
        )
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                currentRating = index + 1
                updateStarDisplay(stars, currentRating)
            }
        }

        // რედაქტირების შემთხვევაში ველების წინასწარ შევსება
        val isEditing = arguments?.getInt(ARG_GAME_ID, -1)?.let { it >= 0 } ?: false
        val argId = arguments?.getInt(ARG_GAME_ID, -1) ?: -1
        val argSortOrder = arguments?.getInt(ARG_GAME_SORT_ORDER, 0) ?: 0

        if (isEditing) {
            binding.editTextTitle.setText(arguments?.getString(ARG_GAME_TITLE, "") ?: "")
            binding.editTextPlatform.setText(arguments?.getString(ARG_GAME_PLATFORM, "") ?: "")

            val status = arguments?.getString(ARG_GAME_STATUS, "Backlog") ?: "Backlog"
            val statusIndex = statuses.indexOf(status)
            if (statusIndex >= 0) {
                binding.spinnerStatus.setSelection(statusIndex)
            }

            currentRating = arguments?.getInt(ARG_GAME_RATING, 3) ?: 3
            updateStarDisplay(stars, currentRating)
        } else {
            updateStarDisplay(stars, currentRating)
        }

        // AlertDialog-ის აგება
        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(if (isEditing) R.string.edit_game else R.string.add_game)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = binding.editTextTitle.text.toString().trim()
                val platform = binding.editTextPlatform.text.toString().trim()
                val status = binding.spinnerStatus.selectedItem.toString()

                if (title.isNotEmpty()) {
                    if (isEditing) {
                        viewModel.updateGame(
                            Game(
                                id = argId,
                                title = title,
                                platform = platform,
                                status = status,
                                rating = currentRating,
                                sortOrder = argSortOrder
                            )
                        )
                    } else {
                        viewModel.insertGame(
                            Game(
                                title = title,
                                platform = platform,
                                status = status,
                                rating = currentRating
                            )
                        )
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)

        // რედაქტირებისას წაშლის ღილაკის დამატება
        if (isEditing) {
            builder.setNeutralButton(R.string.delete) { _, _ ->
                viewModel.deleteGame(
                    Game(
                        id = argId,
                        title = arguments?.getString(ARG_GAME_TITLE, "") ?: "",
                        platform = arguments?.getString(ARG_GAME_PLATFORM, "") ?: "",
                        status = arguments?.getString(ARG_GAME_STATUS, "Backlog") ?: "Backlog",
                        rating = arguments?.getInt(ARG_GAME_RATING, 3) ?: 3,
                        sortOrder = argSortOrder
                    )
                )
            }
        }

        return builder.create()
    }

    private fun updateStarDisplay(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, star ->
            star.setImageResource(
                if (index < rating) R.drawable.ic_star_filled
                else R.drawable.ic_star_empty
            )
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.color.retro_dark)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
