package com.example.wordnotes.ui.addeditword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.databinding.FragmentAddEditWordBinding
import kotlinx.coroutines.launch

class AddEditWordFragment : Fragment() {
    private var _binding: FragmentAddEditWordBinding? = null
    private val binding get() = _binding!!

    private val addEditWordViewModel by viewModels<AddEditWordViewModel> { WordViewModelFactory }
    private val args: AddEditWordFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditWordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addEditWordViewModel.initializeWithWordId(args.wordId)

        binding.apply {
            inputWords.doOnTextChanged { text, _, _, _ ->
                addEditWordViewModel.updateWord { currentWord ->
                    currentWord.copy(word = text.toString())
                }
            }

            inputIpa.doOnTextChanged { text, _, _, _ ->
                addEditWordViewModel.updateWord { currentWord ->
                    currentWord.copy(ipa = text.toString())
                }
            }

            inputMeaning.doOnTextChanged { text, _, _, _ ->
                addEditWordViewModel.updateWord { currentWord ->
                    currentWord.copy(meaning = text.toString())
                }
            }

            buttonSave.setOnClickListener { addEditWordViewModel.saveWord() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addEditWordViewModel.word.collect {
                    updateUi(it)
                }
            }
        }
    }

    private fun updateUi(word: Word) {
        binding.apply {
            inputWords.setText(word.word)
            inputIpa.setText(word.ipa)
            inputMeaning.setText(word.meaning)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}