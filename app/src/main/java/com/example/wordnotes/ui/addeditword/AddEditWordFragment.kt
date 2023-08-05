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
import com.example.wordnotes.OneTimeEventObserver
import com.example.wordnotes.R
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.databinding.FragmentAddEditWordBinding
import com.example.wordnotes.utils.setTextAndMoveCursor
import com.example.wordnotes.utils.setUpToolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

// TODO: Add loading UI
class AddEditWordFragment : Fragment() {
    private var _binding: FragmentAddEditWordBinding? = null
    private val binding get() = _binding!!

    private val addEditWordViewModel: AddEditWordViewModel by viewModels { WordViewModelFactory }
    private val args: AddEditWordFragmentArgs by navArgs()
    private lateinit var partsOfSpeechAdapter: PartsOfSpeechAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditWordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addEditWordViewModel.initializeWithWordId(args.wordId)
        setUpToolbar()
        setUpPartsOfSpeechRecyclerView()
        setViewListeners()
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpToolbar() {
        binding.toolbar.toolbar.apply {
            title = if (args.wordId == null) getString(R.string.add_new_word) else getString(R.string.edit_word)
            inflateMenu(R.menu.add_edit_word_toolbar)
            findNavController().setUpToolbar(this)
        }
    }

    private fun setUpPartsOfSpeechRecyclerView() {
        binding.posRecyclerView.apply {
            itemAnimator = null
            adapter = PartsOfSpeechAdapter(
                data = addEditWordViewModel.englishPartsOfSpeech,
                onItemClicked = { clickedIndex -> addEditWordViewModel.onPosItemClicked(clickedIndex) }
            )
                .also { partsOfSpeechAdapter = it }
        }
    }

    private fun setViewListeners() {
        binding.apply {
            inputWord.doOnTextChanged { text, _, _, _ ->
                addEditWordViewModel.onUpdateWord { it.copy(word = text.toString()) }
            }
            inputIpa.doOnTextChanged { text, _, _, _ ->
                addEditWordViewModel.onUpdateWord { it.copy(ipa = text.toString()) }
            }
            inputMeaning.doOnTextChanged { text, _, _, _ ->
                addEditWordViewModel.onUpdateWord { it.copy(meaning = text.toString()) }
            }
            checkLearning.setOnCheckedChangeListener { _, isChecked ->
                addEditWordViewModel.onUpdateWord { it.copy(isLearning = isChecked) }
            }
        }

        binding.toolbar.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_save -> {
                    addEditWordViewModel.saveWord()
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addEditWordViewModel.uiState.collect { uiState ->
                    updateUi(uiState)
                }
            }
        }

        addEditWordViewModel.wordSavedEvent.observe(viewLifecycleOwner,
            OneTimeEventObserver {
                findNavController().navigate(AddEditWordFragmentDirections.actionAddEditWordFragmentToWordsFragment())
            }
        )
    }

    private fun updateUi(uiState: AddEditWordUiState) {
        binding.apply {
            inputWord.setTextAndMoveCursor(uiState.word.word)
            inputIpa.setTextAndMoveCursor(uiState.word.ipa)
            inputMeaning.setTextAndMoveCursor(uiState.word.meaning)
            checkLearning.apply {
                isChecked = uiState.word.isLearning
                jumpDrawablesToCurrentState()
            }
        }

        partsOfSpeechAdapter.setSelectedIndex(uiState.currentPartOfSpeechIndex)

        uiState.snackBarMessage?.let { showSnackBar(it) }
    }

    private fun showSnackBar(@StringRes messageResId: Int) {
        Snackbar.make(requireView(), messageResId, Snackbar.LENGTH_SHORT).show()
        addEditWordViewModel.snakeBarShown()
    }
}