package com.example.wordnotes.ui.home

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.customviews.materialsearchview.MaterialSearchView
import com.example.customviews.materialsearchview.SearchViewAnimationHelper
import com.example.wordnotes.R
import com.example.wordnotes.databinding.FragmentHomeBinding
import com.example.wordnotes.utils.fadeInStatusBar
import com.example.wordnotes.utils.fadeOutStatusBar
import com.example.wordnotes.utils.hideSoftKeyboard
import com.example.wordnotes.utils.setUpToolbar
import com.example.wordnotes.utils.themeColor
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private var actionMode: ActionMode? = null
    private var inSearching = false
    private var selectedIds = emptySet<String>()

    private var backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = homeViewModel.stopSearching()
    }

    private val voicePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            binding.searchView.listenInput()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setUpRecyclerView()
        setUpSearch()
        setUpViewListeners()
        observeUiStates()
    }

    override fun onStart() {
        super.onStart()
        if (inSearching) {
            // If onStart() is called after returning from AddEditWordFragment, hide bottom nav.
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpToolbar() {
        binding.toolbar.apply {
            inflateMenu(R.menu.home_menu)
            findNavController().setUpToolbar(this)
        }
    }

    private fun setUpRecyclerView() {
        (binding.wordsRecyclerView.adapter as? WordsAdapter)?.submitList(emptyList())
        binding.wordsRecyclerView.apply {
            adapter = WordsAdapter(
                onItemClicked = { if (actionMode != null) homeViewModel.selectItem(wordId = it) else navigateToWordDetailFragment(it) },
                onItemLongClicked = { homeViewModel.onItemLongClicked(wordId = it) }
            )
            addOnScrollListener(OnScrollListener())
        }
    }

    private inner class OnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (actionMode == null) {
                if (dy > SCROLLING_THRESHOLD && binding.fabAddWord.isExtended) {
                    binding.fabAddWord.shrink()
                    slideOutBottomNav(relatedView = arrayOf(binding.fabAddWord))
                } else if (dy < -SCROLLING_THRESHOLD && !binding.fabAddWord.isExtended) {
                    binding.fabAddWord.extend()
                    slideInBottomNav(relatedView = arrayOf(binding.fabAddWord))
                }
            }
        }
    }

    private fun setUpSearch() {
        binding.searchRecyclerView.apply {
            adapter = WordsAdapter(
                onItemClicked = { if (actionMode != null) homeViewModel.selectItem(wordId = it) else navigateToWordDetailFragment(it) },
                onItemLongClicked = { homeViewModel.onItemLongClicked(wordId = it) }
            )
        }

        binding.searchView.addTransitionListener { _, _, newState ->
            if (newState == MaterialSearchView.TransitionState.SHOWING) {
                homeViewModel.startSearching()
            }
            if (newState == MaterialSearchView.TransitionState.HIDING) {
                homeViewModel.stopSearching()
            }
        }

        binding.searchView.setOnQueryTextListener { text ->
            if (binding.searchView.isShowing()) {
                homeViewModel.search(text)
            }
        }

        binding.searchView.setOnVoiceClickedListener {
            when {
                isVoicePermissionAllowed() -> binding.searchView.listenInput()
                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> showRequestPermissionRationale()
                else -> voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    private fun isVoicePermissionAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun showRequestPermissionRationale() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.request_permission))
            .setMessage(getString(R.string.permission_rationale))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            .show()
    }

    private fun setUpViewListeners() {
        binding.fabAddWord.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddEditWordFragment(null))
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
            homeViewModel.refresh()
        }
    }

    private fun observeUiStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.wordsUiState.collect { wordsUiState ->
                    wordsUiState.undoMessage?.let {
                        showUndoSnakeBar(it)
                    }
                    binding.swipeToRefresh.isRefreshing = wordsUiState.isLoading
                    binding.emptyListLayout.root.visibility = if (wordsUiState.isShowEmptyScreen) View.VISIBLE else View.GONE
                    binding.wordsRecyclerView.visibility = if (wordsUiState.isShowEmptyScreen) View.GONE else View.VISIBLE
                    (binding.wordsRecyclerView.adapter as WordsAdapter).submitList(wordsUiState.wordItems)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.actionModeUiState.collect { actionModeUiState ->
                    if (actionModeUiState.isActionMode) {
                        if (actionMode == null) startActionMode()
                        selectedIds = actionModeUiState.selectedIds
                        actionMode?.invalidate()
                    } else if (actionMode != null) {
                        stopActionMode()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.searchUiState.collect { searchUiState ->
                    if (searchUiState.isSearching) {
                        if (!inSearching) startSearching()
                        (binding.searchRecyclerView.adapter as WordsAdapter).submitList(searchUiState.searchResult)
                        backPressedCallback.isEnabled = true
                        // After returning from AddEditWordFragment, change status color to match the search view.
                        requireActivity().window.statusBarColor =
                            requireContext().themeColor(com.google.android.material.R.attr.colorSurfaceContainer)
                    } else if (inSearching) {
                        stopSearching()
                        backPressedCallback.isEnabled = false
                    }
                }
            }
        }
    }

    private fun showUndoSnakeBar(undoMessage: Pair<Int, Int>) {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            getString(undoMessage.second, undoMessage.first),
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.undo) { homeViewModel.undoDeletion() }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event !in arrayOf(DISMISS_EVENT_ACTION, DISMISS_EVENT_CONSECUTIVE)) {
                        homeViewModel.onUndoDismissed()
                    }
                }
            })
            .show()
        homeViewModel.undoSnackBarShown()
    }

    private fun startActionMode() {
        // Hide soft keyboard and disable focus on search input when start ActionMode while searching.
        requireContext().hideSoftKeyboard(binding.searchView)
        binding.searchView.findViewById<EditText>(com.example.customviews.R.id.input_search).isFocusable = false

        actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(WordsActionModeCallback())
        binding.fabAddWord.visibility = View.GONE
        if (!inSearching) {
            requireActivity().run {
                findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.GONE
                window.fadeInStatusBar()
            }
        }
    }

    private fun stopActionMode() {
        // Restore focus on search input when top ActionMode while searching.
        binding.searchView.findViewById<EditText>(com.example.customviews.R.id.input_search).isFocusableInTouchMode = true

        actionMode?.finish()
        actionMode = null
        binding.fabAddWord.visibility = View.VISIBLE
        if (!inSearching) {
            requireActivity().run {
                findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.VISIBLE
                window.fadeOutStatusBar()
            }
        }
    }

    inner class WordsActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.home_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            // User can only edit one item at a time, so hide edit button when there are more than one item are selected.
            menu.findItem(R.id.menu_edit)?.isVisible = selectedIds.size < 2
            mode.title = selectedIds.size.toString()
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = when (item.itemId) {
            R.id.menu_edit -> {
                selectedIds.singleOrNull()?.let { navigateToAddEditWordFragment(it) }
                mode.finish()
                true
            }

            R.id.menu_delete -> homeViewModel.onActionModeMenuDelete()
            R.id.menu_remind -> homeViewModel.onActionModeMenuRemind()
            R.id.menu_select_all -> homeViewModel.onActionModeMenuSelectAll()
            else -> false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            homeViewModel.destroyActionMode()
        }
    }

    private fun startSearching() {
        binding.searchView.show()
        requireActivity().run {
            window.fadeInStatusBar(duration = SearchViewAnimationHelper.CIRCLE_REVEAL_DURATION_MS)
            findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.GONE
        }
        inSearching = true
    }

    private fun stopSearching() {
        binding.searchView.hide()
        requireActivity().run {
            window.fadeOutStatusBar(duration = SearchViewAnimationHelper.CIRCLE_REVEAL_DURATION_MS)
            findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.VISIBLE
        }
        inSearching = false
    }

    private fun navigateToWordDetailFragment(wordId: String) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToWordDetailFragment(wordId))
    }

    private fun navigateToAddEditWordFragment(wordId: String) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddEditWordFragment(wordId))
    }

    private fun slideOutBottomNav(duration: Long = 200, vararg relatedView: View) {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
        ValueAnimator.ofInt(bottomNav.height, 0).apply {
            setDuration(duration)
            addUpdateListener { updatedAnimation ->
                val translationAmount = bottomNav.height.toFloat() - updatedAnimation.animatedValue as Int
                bottomNav.translationY = translationAmount
                relatedView.forEach { it.translationY = translationAmount }
            }
            start()
        }
    }

    private fun slideInBottomNav(duration: Long = 200, vararg relatedView: View) {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
        ValueAnimator.ofInt(0, bottomNav.height).apply {
            setDuration(duration)
            addUpdateListener { updatedAnimation ->
                val translationAmount = bottomNav.height.toFloat() - updatedAnimation.animatedValue as Int
                bottomNav.translationY = translationAmount
                relatedView.forEach { it.translationY = translationAmount }
            }
            start()
        }
    }

    companion object {
        const val SCROLLING_THRESHOLD = 8
    }
}