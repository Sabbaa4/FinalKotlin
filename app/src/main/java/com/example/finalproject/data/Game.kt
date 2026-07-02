package com.example.finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room Entity — "games" ცხრილის მოდელი
@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val platform: String,
    val status: String,    // "Backlog", "Playing", "Finished"
    val rating: Int,       // 1-დან 5-მდე
    val sortOrder: Int = 0 // Drag & Drop-ის თანმიმდევრობისთვის
)
