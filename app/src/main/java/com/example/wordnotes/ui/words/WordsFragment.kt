package com.example.wordnotes.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.wordnotes.OneTimeEventObserver
import com.example.wordnotes.R
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.databinding.FragmentWordsBinding
import com.example.wordnotes.ui.MainActivity
import com.example.wordnotes.utils.setUpToolbar
import com.example.wordnotes.utils.themeColor
import kotlinx.coroutines.launch

// TODO: Add loading UI
// TODO: Add dialog delete items
// TODO: Add empty screen
// TODO: Implement search
// TODO: Optimize observe UI (update the action mode only changes)
class WordsFragment : Fragment() {
    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!

    private val wordsViewModel: WordsViewModel by activityViewModels { WordViewModelFactory }
    private lateinit var wordsAdapter: WordsAdapter
    private var actionMode: ActionMode? = null
    private var selectedCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setUpRecyclerView()
        setUpFab()
        observeUiState()
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as? MainActivity)?.setBottomNavigationVisibility(View.VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpToolbar() {
        binding.toolbar.toolbar.apply {
            title = getString(R.string.words)
            findNavController().setUpToolbar(this)
        }
    }

    private fun setUpRecyclerView() {
        binding.wordsRecyclerView.apply {
            adapter = WordsAdapter(
                onItemClicked = { wordsViewModel.itemClicked(wordId = it) },
                onItemLongClicked = { wordsViewModel.itemLongClicked(wordId = it) }
            )
                .also { wordsAdapter = it }
        }
    }

    private fun setUpFab() {
        binding.fabAddWord.setOnClickListener {
            findNavController().navigate(WordsFragmentDirections.actionWordsFragmentToAddEditWordFragment(null))
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                wordsViewModel.uiState.collect { uiState ->
                    wordsAdapter.setData(uiState.items)
                    updateActionMode(uiState)
                }
            }
        }

        wordsViewModel.clickItemEvent.observe(viewLifecycleOwner,
            OneTimeEventObserver { wordId ->
                findNavController().navigate(WordsFragmentDirections.actionWordsFragmentToWordDetailFragment(wordId))
            }
        )

        wordsViewModel.clickEditItemEvent.observe(viewLifecycleOwner,
            OneTimeEventObserver { wordId ->
                findNavController().navigate(WordsFragmentDirections.actionWordsFragmentToAddEditWordFragment(wordId))
            }
        )
    }

    private fun updateActionMode(uiState: WordsUiState) {
        if (uiState.isActionMode) {
            if (actionMode == null) {
                startActionMode()
            }
            selectedCount = uiState.selectedCount
            actionMode?.invalidate()
        } else {
            stopActionMode()
        }
    }

    private fun startActionMode() {
        actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(WordsActionModeCallback())
        binding.fabAddWord.hide()
        // Change status bar color
        requireActivity().window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = context.themeColor(com.google.android.material.R.attr.colorSurfaceContainer)
        }
        // Hide bottom nav
        (requireActivity() as? MainActivity)?.setBottomNavigationVisibility(View.GONE)
    }

    private fun stopActionMode() {
        actionMode?.finish()
        binding.fabAddWord.show()
        // Reset status bar color
        requireActivity().window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = context.themeColor(com.google.android.material.R.attr.colorSurface)
        }
        // Show bottom nav
        (requireActivity() as? MainActivity)?.setBottomNavigationVisibility(View.VISIBLE)
    }

    inner class WordsActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.words_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Can only edit one item at a time.
            menu.findItem(R.id.menu_edit)?.isVisible = selectedCount < 2
            mode.title = selectedCount.toString()
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = when (item.itemId) {
            R.id.menu_edit -> wordsViewModel.onActionModeMenuEdit()
            R.id.menu_delete -> wordsViewModel.onActionModeMenuDelete()
            R.id.menu_select_all -> wordsViewModel.onActionModeMenuSelectAll()
            else -> false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            wordsViewModel.destroyActionMode()
        }
    }
}