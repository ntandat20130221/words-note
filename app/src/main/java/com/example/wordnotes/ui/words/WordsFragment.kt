package com.example.wordnotes.ui.words

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.customviews.materialsearchview.MaterialSearchView
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
class WordsFragment : Fragment() {
    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!

    private val wordsViewModel: WordsViewModel by activityViewModels { WordViewModelFactory }
    private lateinit var mainActivity: MainActivity
    private lateinit var wordsAdapter: WordsAdapter
    private lateinit var searchAdapter: WordsAdapter
    private var actionMode: ActionMode? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = requireActivity() as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setUpRecyclerView()
        setUpSearch()
        setUpFab()
        setUpViewListeners()
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpToolbar() {
        binding.toolbar.apply {
            title = getString(R.string.words)
            inflateMenu(R.menu.words_menu)
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

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (actionMode == null) {
                        if (dy > 10 && wordsViewModel.isBottomNavVisible) {
                            binding.fabAddWord.shrink()
                            mainActivity.slideOutBottomNav(binding.fabAddWord)
                            wordsViewModel.isBottomNavVisible = false
                        } else if (dy < -10 && !wordsViewModel.isBottomNavVisible) {
                            binding.fabAddWord.extend()
                            mainActivity.slideInBottomNav(binding.fabAddWord)
                            wordsViewModel.isBottomNavVisible = true
                        }
                    }
                }
            })
        }
    }

    private fun setUpSearch() {
        binding.searchRecyclerView.apply {
            adapter = WordsAdapter(
                onItemClicked = { wordsViewModel.itemClicked(wordId = it) },
                onItemLongClicked = { true }
            )
                .also { searchAdapter = it }
        }

        binding.searchView.addTransitionListener { _, _, newState ->
            if (newState == MaterialSearchView.TransitionState.HIDDEN) {
                wordsViewModel.stopSearching()
            }
        }

        binding.searchView.setOnQueryTextListener { text ->
            if (binding.searchView.isShowing()) {
                wordsViewModel.search(text)
            }
        }
    }

    private fun setUpFab() {
        binding.fabAddWord.setOnClickListener {
            findNavController().navigate(WordsFragmentDirections.actionWordsFragmentToAddEditWordFragment(null))
        }
    }

    private fun setUpViewListeners() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_search -> {
                    wordsViewModel.startSearching()
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                wordsViewModel.uiState.collect { uiState ->
                    wordsAdapter.setData(uiState.items)
                    updateActionMode(uiState)
                    updateSearching(uiState)
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
                onStartActionMode()
            }
            actionMode?.invalidate()
        } else {
            onStopActionMode()
        }
    }

    private fun onStartActionMode() {
        actionMode = mainActivity.startSupportActionMode(WordsActionModeCallback())

        binding.fabAddWord.apply {
            visibility = View.GONE
            if (!isExtended) extend()
        }
        mainActivity.setBottomNavVisibility(View.GONE)
        mainActivity.resetBottomNavAnimation(binding.fabAddWord)
        wordsViewModel.isBottomNavVisible = false

        // Change status bar color
        requireActivity().window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = context.themeColor(com.google.android.material.R.attr.colorSurfaceContainer)
        }
    }

    private fun onStopActionMode() {
        actionMode?.finish()

        binding.fabAddWord.visibility = View.VISIBLE
        mainActivity.setBottomNavVisibility(View.VISIBLE)
        wordsViewModel.isBottomNavVisible = true

        // Reset status bar color
        requireActivity().window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = context.themeColor(com.google.android.material.R.attr.colorSurface)
        }
    }

    inner class WordsActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.words_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            // User can only edit one item at a time.
            menu.findItem(R.id.menu_edit)?.isVisible = wordsViewModel.selectedCount < 2
            mode.title = wordsViewModel.selectedCount.toString()
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = when (item.itemId) {
            R.id.menu_edit -> wordsViewModel.onActionModeMenuEdit()
            R.id.menu_delete -> wordsViewModel.onActionModeMenuDelete()
            R.id.menu_remind -> wordsViewModel.onActionModeMenuRemind()
            R.id.menu_select_all -> wordsViewModel.onActionModeMenuSelectAll()
            else -> false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            wordsViewModel.destroyActionMode()
        }
    }

    private fun updateSearching(uiState: WordsUiState) {
        if (uiState.isSearching) {
            if (!binding.searchView.isShowing()) {
                startSearching()
            }
            searchAdapter.setData(uiState.searchResult)
        } else if (binding.searchView.isShowing()) {
            stopSearching()
        }
    }

    private fun startSearching() {
        mainActivity.setBottomNavVisibility(View.GONE)
        binding.searchView.show()
    }

    private fun stopSearching() {
        mainActivity.setBottomNavVisibility(View.VISIBLE)
        binding.searchView.hide()
    }
}