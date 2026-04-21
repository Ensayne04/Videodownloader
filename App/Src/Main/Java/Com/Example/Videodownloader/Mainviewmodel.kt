package com.example.videodownloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.UUID

class MainViewModel : ViewModel() {
    private val _historyList = MutableLiveData<List<HistoryItem>>(emptyList())
    val historyList: LiveData<List<HistoryItem>> = _historyList

    fun addDownload(id: UUID, url: String) {
        val current = _historyList.value?.toMutableList() ?: mutableListOf()
        current.add(0, HistoryItem(id, url, "Queued")) // Add to top
        _historyList.value = current
    }

    fun updateStatus(id: UUID, status: String, progress: Int = 0) {
        val current = _historyList.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = current[index]
            item.status = status
            item.progress = progress
            _historyList.value = current
        }
    }
}
