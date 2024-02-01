package com.example.wordnotes.ui.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.wordnotes.databinding.FragmentLearningBinding
import com.example.wordnotes.utils.setUpToolbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LearningFragment : Fragment() {
    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setUpViewListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpToolbar() {
        binding.toolbar.toolbar.apply {
            findNavController().setUpToolbar(this)
        }
    }

    private fun setUpViewListeners() {
        binding.buttonWord.setOnClickListener {
            findNavController().navigate(LearningFragmentDirections.actionLearningFragmentToFlashCardFragment(FlashCardFragment.TYPE_WORD))
        }

        binding.buttonMeaning.setOnClickListener {
            findNavController().navigate(LearningFragmentDirections.actionLearningFragmentToFlashCardFragment(FlashCardFragment.TYPE_MEANING))
        }

        binding.buttonMix.setOnClickListener {
            findNavController().navigate(LearningFragmentDirections.actionLearningFragmentToFlashCardFragment(FlashCardFragment.TYPE_MIX))
        }
    }
}