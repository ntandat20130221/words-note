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
import com.example.wordnotes.R
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.databinding.FragmentWordsBinding
import kotlinx.coroutines.launch

class WordsFragment : Fragment() {
    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!

    private val wordsViewModel by activityViewModels<WordsViewModel> { WordViewModelFactory }
    private lateinit var wordsAdapter: WordsAdapter

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

    private fun setUpRecyclerView() {
        binding.wordsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            wordsAdapter = WordsAdapter(
                onItemClicked = { handleOnWordItemClicked(wordId = it) },
                onItemLongClicked = { handleOnWordItemLongClicked(wordId = it) }
            ).also { adapter = it }
        }
    }

    private fun setUpFab() {
        binding.fabAddWord.setOnClickListener {
            navigateToAddNewWord()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                wordsViewModel.words.collect {
                    updateUi(it)
                }
            }
        }
    }

    private fun updateUi(words: List<Word>) {
        wordsAdapter.setData(words)
    }

    private fun handleOnWordItemClicked(wordId: String) {
        navigateToEditWord(wordId)
    }

    private fun handleOnWordItemLongClicked(wordId: String): Boolean {
        (requireActivity() as AppCompatActivity).startSupportActionMode(WordsActionModeCallback())
        return true
    }

    private fun navigateToAddNewWord() {
        findNavController().navigate(WordsFragmentDirections.actionShowAddEditWordFragment(null))
    }

    private fun navigateToEditWord(wordId: String) {
        findNavController().navigate(WordsFragmentDirections.actionShowAddEditWordFragment(wordId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class WordsActionModeCallback : ActionMode.Callback {
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.words_action_mode, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {

    }
}