package com.makkor.eb.ui.history

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import androidx.appsearch.app.AppSearchBatchResult
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.RemoveByDocumentIdRequest
import androidx.appsearch.app.SearchResult
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.app.SearchSpec.RANKING_STRATEGY_CREATION_TIMESTAMP
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.localstorage.LocalStorage
import androidx.appsearch.platformstorage.PlatformStorage
import androidx.concurrent.futures.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class HistorySearchManager(context: Context, coroutineScope: CoroutineScope) {
    private val isInitialized: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private lateinit var appSearchSession: AppSearchSession

    init {
        coroutineScope.launch {
            appSearchSession = if (SDK_INT >= 31) {
                PlatformStorage.createSearchSessionAsync(
                    PlatformStorage.SearchContext.Builder(
                        context,
                        DATABASE_NAME
                    ).build()
                ).await()
            } else {
                LocalStorage.createSearchSessionAsync(
                    LocalStorage.SearchContext.Builder(
                        context,
                        DATABASE_NAME
                    ).build()
                ).await()
            }
            try {
                val setSchemaRequest =
                    SetSchemaRequest.Builder().addDocumentClasses(
                        History::class.java
                    ).build()
                appSearchSession.setSchemaAsync(setSchemaRequest).await()
                isInitialized.value = true
                awaitCancellation()
            } finally {
                appSearchSession.close()
            }


        }

    }
    suspend fun addHistory(history: History): AppSearchBatchResult<String, Void> {
        awaitInitialization()
        val request = PutDocumentsRequest.Builder().addDocuments(history).build()
        return appSearchSession.putAsync(request).await()
    }
    suspend fun queryLatestHistories(query: String): List<SearchResult> {
        awaitInitialization()

        val searchSpec = SearchSpec.Builder()
            .setRankingStrategy(RANKING_STRATEGY_CREATION_TIMESTAMP)
            .setSnippetCount(10)
            .build()

        val searchResults = appSearchSession.search(query, searchSpec)
        return searchResults.nextPageAsync.await()
    }

    suspend fun removeHistory(
        namespace: String,
        id: String
    ): AppSearchBatchResult<String, Void> {
        awaitInitialization()

        val request =
            RemoveByDocumentIdRequest.Builder(namespace).addIds(id).build()
        return appSearchSession.removeAsync(request).await()
    }
    private suspend fun awaitInitialization() {
        if (!isInitialized.value) {
            isInitialized.first { it }
        }
    }
    companion object {
        private const val DATABASE_NAME = "historyDatabase"
    }
}