package com.makkor.eb.ui.history

import android.app.Application
import androidx.appsearch.app.SearchResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val _errorMessageLiveData = MutableLiveData<String?>()
    val errorMessageLiveData: LiveData<String?> = _errorMessageLiveData
    private val _historyLiveData: MutableLiveData<List<SearchResult>> =
        MutableLiveData(mutableListOf())
    private val historyLiveData: LiveData<List<SearchResult>> = _historyLiveData
    private val historySearchManager: HistorySearchManager =
        HistorySearchManager(getApplication(), viewModelScope)

    fun addHistory(text: String) {
        val id = UUID.randomUUID().toString()
        val history = History(namespace = "lol", id = id, text = text)
        viewModelScope.launch {
            val result = historySearchManager.addHistory(history)
            if (!result.isSuccess) {
                _errorMessageLiveData.postValue("Failed to add element with id: $id and text: $text")
            }
            queryHistories()
        }
    }
    fun removeHistory(namespace: String, id: String) {
        viewModelScope.launch {
            val result = historySearchManager.removeHistory(namespace, id)
            if (!result.isSuccess) {
                _errorMessageLiveData.postValue(
                    "Failed to remove: $namespace with id: $id"
                )
            }

            queryHistories()
        }
    }
    fun queryHistories(query: String = ""): LiveData<List<SearchResult>> {
        viewModelScope.launch {
            val resultHistories = historySearchManager.queryLatestHistories(query)
            _historyLiveData.postValue(resultHistories)
        }
        return historyLiveData
    }

    class HistoryViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}