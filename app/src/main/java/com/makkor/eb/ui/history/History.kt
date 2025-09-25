package com.makkor.eb.ui.history
import androidx.appsearch.annotation.Document
import androidx.appsearch.app.AppSearchSchema

@Document
data class History(
    @Document.Namespace
    val namespace: String,
    @Document.Id
    val id: String,
    @Document.StringProperty(indexingType = AppSearchSchema
        .StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val text: String
)