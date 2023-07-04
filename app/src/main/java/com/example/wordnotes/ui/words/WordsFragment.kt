package com.example.wordnotes.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.databinding.FragmentWordsBinding
import kotlinx.coroutines.launch

class WordsFragment : Fragment() {
    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!

    private val wordsViewModel by viewModels<WordsViewModel> { WordViewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWordsRecyclerView()
        setUpFab()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                wordsViewModel.words.collect {
                    updateUi(it)
                }
            }
        }
    }

    private fun updateUi(words: List<Word>) {
        binding.apply {
            wordsRecyclerView.adapter = WordsAdapter(words)
        }
    }

    private fun setUpWordsRecyclerView() {
        binding.wordsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setUpFab() {
        binding.fabAddWords.setOnClickListener {
            navigateToAddNewWords()
        }
    }

    private fun navigateToAddNewWords() {
        findNavController().navigate(WordsFragmentDirections.actionShowAddEditWordFragment(null))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}