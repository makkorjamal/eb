package com.makkor.eb.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.makkor.eb.R
import com.makkor.eb.databinding.FragmentHistoryBinding


class HistoryFragment : Fragment() {

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModel.HistoryViewModelFactory(requireActivity().application)
    }

    private lateinit var progressSpinner: ProgressBar
    private lateinit var notesList: RecyclerView
    private lateinit var noNotesMessage: TextView
    private lateinit var insertNoteButton: FloatingActionButton
    private lateinit var historyAdapter: HistoryListItemAdapter
    private var _binding: FragmentHistoryBinding? = null
    private lateinit var searchView: SearchView
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.top_menu, menu)
                val searchMenuItem = menu.findItem(R.id.app_bar_search)
                searchMenuItem?.isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.app_bar_search -> {
                        // clearCompletedTasks()
                        initQueryListener()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        ViewModelProvider(this).get(HistoryViewModel::class.java)
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        progressSpinner = root.findViewById(R.id.progress_spinner)
        notesList = root.findViewById(R.id.notes_list)
        noNotesMessage = root.findViewById(R.id.no_notes_message)
        insertNoteButton = root.findViewById(R.id.fab)

        initAddNoteButtonListener()
        initNoteListView()

        historyViewModel.queryHistories().observe(
            requireActivity(),
            {
                historyAdapter.submitList(it)
                progressSpinner.visibility = View.GONE
                if (it.isEmpty()) {
                    notesList.visibility = View.GONE
                    noNotesMessage.visibility = View.VISIBLE
                } else {
                    notesList.visibility = View.VISIBLE
                    noNotesMessage.visibility = View.GONE
                }
            }
        )

        historyViewModel.errorMessageLiveData.observe(requireActivity(), {
            it?.let {
                Toast.makeText(requireActivity(), it, LENGTH_LONG).show()
            }
        })
        return root
    }
    private fun initQueryListener() {
        //searchView.queryHint = getString(R.string.search_bar_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                historyViewModel.queryHistories(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // This resets the notes list to display all notes if the query is
                // cleared.
                if (newText.isEmpty()) historyViewModel.queryHistories()
                return false
            }
        })
    }
    private fun initAddNoteButtonListener() {
        insertNoteButton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireActivity())
            dialogBuilder.setView(R.layout.add_history_dialog)
                .setCancelable(false)
                .setPositiveButton(R.string.add_note_dialog_save) { dialog, _ ->
                    val addNoteDialogView = dialog as AlertDialog
                    val noteEditText =
                        addNoteDialogView.findViewById(R.id.add_history_text) as EditText?
                    val hText = noteEditText?.text.toString()
                    progressSpinner.visibility = View.VISIBLE
                    noNotesMessage.visibility = View.GONE
                    notesList.visibility = View.GONE
                    historyViewModel.addHistory(hText)
                }
                .setNegativeButton(R.string.add_note_dialog_save) { dialog, _ ->
                    dialog.cancel()
                }
            val alert = dialogBuilder.create()
            alert.setTitle(R.string.add_note_dialog_save)
            alert.show()
        }
    }
    private fun initNoteListView() {
        historyAdapter = HistoryListItemAdapter {
            if (it != null) {
                val history = it.genericDocument.toDocumentClass(History::class.java)
                historyViewModel.removeHistory(history.namespace, history.id)
            }
        }
        notesList.adapter = historyAdapter
        notesList.addItemDecoration(
            DividerItemDecoration(
                requireActivity(),
                LinearLayoutManager.VERTICAL
            )
        )
        notesList.layoutManager = LinearLayoutManager(requireActivity())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}