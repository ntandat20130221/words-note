package com.example.wordnotes.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wordnotes.databinding.FragmentWordsBinding

class WordsFragment : Fragment() {
    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!

    private val wordsViewModel: WordsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWordsRecyclerView()
    }

    private fun setUpWordsRecyclerView() {
        binding.wordsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = WordsAdapter(wordsViewModel.words)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}