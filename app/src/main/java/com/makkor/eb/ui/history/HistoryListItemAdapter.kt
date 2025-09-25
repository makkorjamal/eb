package com.makkor.eb.ui.history

import android.graphics.Typeface.BOLD
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appsearch.app.SearchResult
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.makkor.eb.R
import com.makkor.eb.ui.history.History

class HistoryListItemAdapter(private val onDelete: (SearchResult?) -> Unit) :
    ListAdapter<SearchResult, HistoryListItemAdapter.HistoryViewHolder>(HISTORIES_COMPARATOR) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, /*attachToRoot=*/false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position), onDelete)
    }

    inner class HistoryViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        val noteTextView: TextView = view.findViewById(R.id.note_text)
        val noteDeleteButtonView: Button = view.findViewById(R.id.note_delete_button)

        fun bind(searchResult: SearchResult, onDelete: (SearchResult?) -> Unit) {
            val history = searchResult.genericDocument.toDocumentClass(History::class.java)
            val stringBuilder = SpannableStringBuilder(history.text)

            searchResult.matchInfos.forEach {
                if (it.propertyPath == TEXT_PROPERTY_PATH)
                    stringBuilder.setSpan(
                        StyleSpan(BOLD),
                        it.exactMatchRange.start,
                        it.exactMatchRange.end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
            }

            noteTextView.text = stringBuilder

            noteDeleteButtonView.setOnClickListener { onDelete(searchResult) }
        }
    }

    companion object {
        private const val TEXT_PROPERTY_PATH = "text"

        private val HISTORIES_COMPARATOR = object : DiffUtil.ItemCallback<SearchResult>() {
            override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
                val oi = oldItem.genericDocument.toDocumentClass(History::class.java)
                val ni = newItem.genericDocument.toDocumentClass(History::class.java)
                return oi.id === ni.id &&
                        oi.namespace === ni.namespace
            }

            override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
                return oldItem.genericDocument == newItem.genericDocument
            }
        }
    }

}