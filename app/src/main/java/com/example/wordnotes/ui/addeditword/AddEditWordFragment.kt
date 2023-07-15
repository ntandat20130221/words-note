package com.example.wordnotes.ui.addeditword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.wordnotes.OneTimeEventObserver
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.databinding.FragmentAddEditWordBinding
import com.google.android.material.snackbar.Snackbar
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
        setUpNavigation()
        setViewListeners()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { addEditWordViewModel.word.collect { updateUi(it) } }
                launch {
                    addEditWordViewModel.snackBarMessage.collect { messageResId ->
                        if (messageResId != 0) {
                            showSnackBar(messageResId)
                        }
                    }
                }
            }
        }

        addEditWordViewModel.taskUpdatedEvent.observe(viewLifecycleOwner,
            OneTimeEventObserver {
                findNavController().navigate(AddEditWordFragmentDirections.actionAddEditWordFragmentToWordsFragment())
            }
        )
    }

    private fun setUpNavigation() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setViewListeners() {
        binding.apply {
            inputWords.doOnTextChanged { text, _, _, _ ->
                addEditWordViewModel.onUserUpdatesWord { currentWord ->
                    currentWord.copy(word = text.toString())
                }
            }

            inputPos.doOnTextChanged { text, _, _, _ ->
                addEditWordViewModel.onUserUpdatesWord { currentWord ->
                    currentWord.copy(pos = text.toString())
                }
            }

            inputIpa.doOnTextChanged { text, _, _, _ ->
                addEditWordViewModel.onUserUpdatesWord { currentWord ->
                    currentWord.copy(ipa = text.toString())
                }
            }

            inputMeaning.doOnTextChanged { text, _, _, _ ->
                addEditWordViewModel.onUserUpdatesWord { currentWord ->
                    currentWord.copy(meaning = text.toString())
                }
            }

            checkLearning.setOnCheckedChangeListener { _, isChecked ->
                addEditWordViewModel.onUserUpdatesWord { currentWord ->
                    currentWord.copy(isLearning = isChecked)
                }
            }

            buttonSave.setOnClickListener { addEditWordViewModel.saveWord() }
        }
    }

    private fun updateUi(word: Word) {
        binding.apply {
            if (word.word != inputWords.text.toString()) {
                inputWords.apply {
                    setText(word.word)
                    setSelection(word.word.length)
                }
            }
            if (word.pos != inputPos.text.toString()) {
                inputPos.apply {
                    setText(word.pos)
                    setSelection(word.pos.length)
                }
            }
            if (word.ipa != inputIpa.text.toString()) {
                inputIpa.apply {
                    setText(word.ipa)
                    setSelection(word.ipa.length)
                }
            }
            if (word.meaning != inputMeaning.text.toString()) {
                inputMeaning.apply {
                    setText(word.meaning)
                    setSelection(word.meaning.length)
                }
            }
            checkLearning.apply {
                isChecked = word.isLearning
                jumpDrawablesToCurrentState()
                // TODO("Make jumpDrawablesToCurrentState() only when the first population")
            }
        }
    }

    private fun showSnackBar(@StringRes messageResId: Int) {
        Snackbar.make(requireView(), messageResId, Snackbar.LENGTH_SHORT).show()
    }
}