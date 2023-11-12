package com.example.wordnotes.ui.words

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.customviews.materialsearchview.MaterialSearchView
import com.example.customviews.materialsearchview.SearchViewAnimationHelper
import com.example.wordnotes.OneTimeEventObserver
import com.example.wordnotes.R
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.databinding.FragmentWordsBinding
import com.example.wordnotes.ui.MainActivity
import com.example.wordnotes.utils.fadeInStatusBar
import com.example.wordnotes.utils.fadeOutStatusBar
import com.example.wordnotes.utils.setUpToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class WordsFragment : Fragment() {
    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!

    private val wordsViewModel: WordsViewModel by activityViewModels { WordViewModelFactory }
    private lateinit var mainActivity: MainActivity
    private lateinit var wordsAdapter: WordsAdapter
    private lateinit var searchAdapter: WordsAdapter

    private var actionMode: ActionMode? = null
    private var inSearching = false
    private var backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = wordsViewModel.stopSearching()
    }
    private val voicePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            binding.searchView.listenInput()
        }
    }

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
        setUpViewListeners()
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpToolbar() {
        binding.toolbar.apply {
            inflateMenu(R.menu.words_menu)
            findNavController().setUpToolbar(this)
        }
    }

    private fun setUpRecyclerView() {
        binding.wordsRecyclerView.apply {
            wordsAdapter = WordsAdapter(
                onItemClicked = { wordsViewModel.itemClicked(wordId = it) },
                onItemLongClicked = { wordsViewModel.itemLongClicked(wordId = it) })
            adapter = wordsAdapter
            addOnScrollListener(OnScrollListener())
        }
    }

    private inner class OnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (actionMode == null) {
                if (dy > SCROLLING_THRESHOLD && binding.fabAddWord.isExtended) {
                    binding.fabAddWord.shrink()
                    mainActivity.slideOutBottomNav(relatedView = arrayOf(binding.fabAddWord))
                } else if (dy < -SCROLLING_THRESHOLD && !binding.fabAddWord.isExtended) {
                    binding.fabAddWord.extend()
                    mainActivity.slideInBottomNav(relatedView = arrayOf(binding.fabAddWord))
                }
            }
        }
    }

    private fun setUpSearch() {
        binding.searchRecyclerView.apply {
            searchAdapter = WordsAdapter(
                onItemClicked = { wordsViewModel.itemClicked(wordId = it) },
                onItemLongClicked = { wordsViewModel.itemLongClicked(wordId = it) })
            adapter = searchAdapter
        }

        binding.searchView.addTransitionListener { _, _, newState ->
            if (newState == MaterialSearchView.TransitionState.SHOWING) {
                wordsViewModel.startSearching()
            }
            if (newState == MaterialSearchView.TransitionState.HIDING) {
                wordsViewModel.stopSearching()
            }
        }

        binding.searchView.setOnQueryTextListener { text ->
            if (binding.searchView.isShowing()) {
                wordsViewModel.search(text)
            }
        }

        binding.searchView.setOnVoiceClickedListener {
            when {
                isVoicePermissionAllowed() -> binding.searchView.listenInput()
                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> showInContextUI()
                else -> voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    private fun isVoicePermissionAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun showInContextUI() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.request_permission))
            .setMessage(getString(R.string.permission_rationale))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            .show()
    }

    private fun setUpViewListeners() {
        binding.fabAddWord.setOnClickListener {
            findNavController().navigate(WordsFragmentDirections.actionWordsFragmentToAddEditWordFragment(null))
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_search -> {
                    binding.searchView.show()
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }

        binding.swipeToRefresh.setOnRefreshListener {
            wordsViewModel.refresh()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                wordsViewModel.uiState.collect { uiState ->
                    updateRecyclerView(uiState)
                    updateActionMode(uiState)
                    updateSearching(uiState)
                    binding.swipeToRefresh.isRefreshing = uiState.isLoading
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

        wordsViewModel.showUndoEvent.observe(viewLifecycleOwner,
            OneTimeEventObserver { amount ->
                Snackbar.make(mainActivity.findViewById(android.R.id.content), getString(R.string.deleted_template, amount), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) { wordsViewModel.undoDeletion() }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (DISMISS_EVENT_TIMEOUT == event) {
                                wordsViewModel.undoDismissed()
                            }
                        }
                    })
                    .show()
            }
        )
    }

    private fun updateRecyclerView(uiState: WordsUiState) {
        if (uiState.items.isEmpty() && !uiState.firstEmit && !uiState.isLoading) {
            binding.emptyListLayout.root.visibility = View.VISIBLE
            binding.wordsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyListLayout.root.visibility = View.GONE
            binding.wordsRecyclerView.visibility = View.VISIBLE
            wordsAdapter.setData(uiState.items)
        }
    }

    private fun updateActionMode(uiState: WordsUiState) {
        if (uiState.isActionMode) {
            if (actionMode == null) startActionMode()
            actionMode?.invalidate()
        } else if (actionMode != null) {
            stopActionMode()
        }
    }

    private fun startActionMode() {
        actionMode = mainActivity.startSupportActionMode(WordsActionModeCallback())
        binding.fabAddWord.visibility = View.GONE
        if (!inSearching) {
            mainActivity.setBottomNavVisibility(View.GONE)
            requireActivity().window.fadeInStatusBar()
        }
    }

    private fun stopActionMode() {
        actionMode?.finish()
        actionMode = null
        binding.fabAddWord.visibility = View.VISIBLE
        if (!inSearching) {
            mainActivity.setBottomNavVisibility(View.VISIBLE)
            requireActivity().window.fadeOutStatusBar()
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
            wordsViewModel.destroyActionMode()
        }
    }

    private fun updateSearching(uiState: WordsUiState) {
        if (uiState.isSearching) {
            if (!inSearching) startSearching()
            searchAdapter.setData(uiState.searchResult)
            backPressedCallback.isEnabled = true
        } else if (inSearching) {
            stopSearching()
            backPressedCallback.isEnabled = false
        }
    }

    private fun startSearching() {
        binding.searchView.show()
        requireActivity().window.fadeInStatusBar(duration = SearchViewAnimationHelper.CIRCLE_REVEAL_DURATION_MS)
        mainActivity.setBottomNavVisibility(View.GONE)
        inSearching = true
    }

    private fun stopSearching() {
        binding.searchView.hide()
        requireActivity().window.fadeOutStatusBar(duration = SearchViewAnimationHelper.CIRCLE_REVEAL_DURATION_MS)
        mainActivity.setBottomNavVisibility(View.VISIBLE)
        inSearching = false
    }

    companion object {
        const val SCROLLING_THRESHOLD = 8
    }
}