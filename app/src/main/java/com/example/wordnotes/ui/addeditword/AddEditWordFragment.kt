package com.example.wordnotes.ui.addeditword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
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
import com.example.wordnotes.databinding.FragmentAddEditWordBinding
import com.example.wordnotes.utils.hideSoftKeyboard
import com.example.wordnotes.utils.setUpToolbar
import com.example.wordnotes.utils.showSoftKeyboard
import com.example.wordnotes.utils.themeColor
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditWordFragment : Fragment() {
    private var _binding: FragmentAddEditWordBinding? = null
    private val binding get() = _binding!!

    private val addEditWordViewModel: AddEditWordViewModel by viewModels()
    private val args: AddEditWordFragmentArgs by navArgs()
    private lateinit var partsOfSpeechAdapter: PartsOfSpeechAdapter
    private var originalSoftInputMode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addEditWordViewModel.initializeWithWordId(args.wordId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditWordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setViewListeners()
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpViews() {
        setUpToolbar()
        setUpPartsOfSpeechRecyclerView()
        binding.inputIpa.showSoftInputOnFocus = false
        // Change the status bar color to default if this fragment is navigated while searching.
        requireActivity().window.statusBarColor = requireContext().themeColor(com.google.android.material.R.attr.colorSurface)
    }

    private fun setUpToolbar() {
        binding.toolbar.toolbar.apply {
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
        binding.inputIpa.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // Connect to IPAKeyboard.
                binding.ipaKeyboard.setInputConnection(binding.inputIpa.onCreateInputConnection(EditorInfo()))
                // Prevent flickering effect when hiding system soft keyboard then showing IPAKeyboard.
                originalSoftInputMode = requireActivity().window.attributes.softInputMode
                requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                // Hide the system soft keyboard if it is showing.
                requireContext().hideSoftKeyboard(view)
                // Show the IPAKeyboard.
                binding.ipaKeyboard.visibility = View.VISIBLE
            } else {
                // Restore the original soft input method.
                originalSoftInputMode?.let { requireActivity().window.setSoftInputMode(it) }
                // Hide the IPAKeyboard.
                binding.ipaKeyboard.visibility = View.GONE
            }
        }

        // For fixing bug where soft keyboard was not showed after pressing the 'done' key on IPA keyboard.
        binding.inputMeaning.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                requireContext().showSoftKeyboard(view)
            }
        }

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
            checkRemind.setOnCheckedChangeListener { _, isChecked ->
                addEditWordViewModel.onUpdateWord { it.copy(isRemind = isChecked) }
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
                findNavController().navigateUp()
            }
        )
    }

    private fun updateUi(uiState: AddEditWordUiState) {
        binding.apply {
            if (inputWord.text.toString() != uiState.word.word) inputWord.setText(uiState.word.word)
            if (inputIpa.text.toString() != uiState.word.ipa) inputIpa.setText(uiState.word.ipa)
            if (inputMeaning.text.toString() != uiState.word.meaning) inputMeaning.setText(uiState.word.meaning)
            if (checkRemind.isChecked != uiState.word.isRemind) checkRemind.apply {
                isChecked = uiState.word.isRemind
                jumpDrawablesToCurrentState()
            }
        }

        partsOfSpeechAdapter.setSelectedIndex(uiState.currentPosIndex)

        uiState.snackBarMessage?.let { showSnackBar(it) }

        if (uiState.isInputFocus) {
            binding.inputWord.requestFocus()
            requireContext().showSoftKeyboard(binding.inputWord)
            addEditWordViewModel.gainedFocus()
        }
    }

    private fun showSnackBar(@StringRes messageResId: Int) {
        Snackbar.make(requireView(), messageResId, Snackbar.LENGTH_SHORT).show()
        addEditWordViewModel.snakeBarShown()
    }
}