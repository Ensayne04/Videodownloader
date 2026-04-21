package com.example.videodownloader

import java.util.UUID

data class HistoryItem(
    val id: UUID,
    val url: String,
    var status: String,
    var progress: Int = 0
)
