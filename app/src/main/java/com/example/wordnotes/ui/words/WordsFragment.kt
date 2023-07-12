package com.example.wordnotes.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wordnotes.EventObserver
import com.example.wordnotes.OneTimeEventObserver
import com.example.wordnotes.R
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.databinding.FragmentWordsBinding
import kotlinx.coroutines.launch

class WordsFragment : Fragment() {
    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!

    private val wordsViewModel by activityViewModels<WordsViewModel> { WordViewModelFactory }
    private lateinit var wordsAdapter: WordsAdapter
    private var actionMode: ActionMode? = null
    private var selectedCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        setUpFab()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpRecyclerView() {
        binding.wordsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            wordsAdapter = WordsAdapter(
                onItemClicked = { wordsViewModel.itemClicked(wordId = it) },
                onItemLongClicked = { wordsViewModel.itemLongClicked(wordId = it) }
            ).also { adapter = it }
        }
    }

    private fun setUpFab() {
        binding.fabAddWord.setOnClickListener {
            findNavController().navigate(WordsFragmentDirections.actionShowAddEditWordFragment(null))
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                wordsViewModel.uiState.collect { uiState ->
                    wordsAdapter.setData(uiState.words)
                    selectedCount = uiState.selectedCount
                    actionMode?.invalidate()
                }
            }
        }

        wordsViewModel.clickItemEvent.observe(viewLifecycleOwner,
            OneTimeEventObserver { wordId ->
                findNavController().navigate(WordsFragmentDirections.actionShowAddEditWordFragment(wordId))
            }
        )

        wordsViewModel.actionModeEvent.observe(viewLifecycleOwner,
            EventObserver { actionModeState ->
                when (actionModeState) {
                    ActionModeState.STARTED -> {
                        startActionMode()
                        binding.fabAddWord.hide()
                    }

                    ActionModeState.STOPPED -> {
                        actionMode?.finish()
                        binding.fabAddWord.show()
                    }
                }
            }
        )

        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.words_fragment) {
                wordsViewModel.destroyActionMode()
            }
        }
    }

    private fun startActionMode() {
        actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(WordsActionModeCallback())
    }

    inner class WordsActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.words_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Can only edit one item at a time.
            menu.findItem(R.id.menu_edit)?.isVisible = selectedCount < 2
            mode.title = getString(R.string.selected_template, selectedCount)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_edit -> {
                    wordsViewModel.onActionModeMenuEdit()
                    return true
                }

                R.id.menu_delete -> {
                    wordsViewModel.onActionModeMenuDelete()
                    return true
                }

                R.id.menu_select_all -> {
                    wordsViewModel.onActionModeMenuSelectAll()
                    return true
                }
            }

            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            wordsViewModel.destroyActionMode()
        }
    }
}