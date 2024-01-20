package com.example.wordnotes.ui.worddetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.wordnotes.OneTimeEventObserver
import com.example.wordnotes.R
import com.example.wordnotes.databinding.FragmentWordDetailBinding
import com.example.wordnotes.ui.TextToSpeechService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WordDetailFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentWordDetailBinding? = null
    private val binding get() = _binding!!

    private val wordDetailViewModel: WordDetailViewModel by viewModels()
    private val args: WordDetailFragmentArgs by navArgs()
    private lateinit var wordId: String
    private val ttsService: TextToSpeechService by lazy { TextToSpeechService(requireContext(), lifecycleScope) }

    override fun getTheme() = R.style.BottomSheetDialogStyle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wordDetailViewModel.initializeWithWordId(args.wordId.also { wordId = it })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWordDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
        setActionListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsService.shutdown()
        _binding = null
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                wordDetailViewModel.uiState.collect { uiState ->
                    binding.apply {
                        textWord.text = uiState.word
                        textIpa.text = uiState.ipa
                        textPos.text = uiState.pos
                        textMeaning.text = uiState.meaning

                        uiState.ipa.ifEmpty { textIpa.visibility = View.GONE }

                        imageRemind.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                if (uiState.isRemind) R.drawable.ic_alarm_off else R.drawable.ic_alarm
                            )
                        )
                        textRemind.setText(if (uiState.isRemind) R.string.stop_remind else R.string.pref_title_remind)
                    }
                }
            }
        }

        wordDetailViewModel.dismissEvent.observe(viewLifecycleOwner,
            OneTimeEventObserver {
                dismiss()
            }
        )
    }

    private fun setActionListeners() {
        binding.actionDelete.setOnClickListener {
            wordDetailViewModel.deleteWord()
        }

        binding.actionEdit.setOnClickListener {
            findNavController().navigate(WordDetailFragmentDirections.actionWordDetailFragmentToAddEditWordFragment(wordId))
        }

        binding.actionRemind.setOnClickListener {
            wordDetailViewModel.toggleRemind()
        }

        binding.imageSpeech.setOnClickListener {
            ttsService.speak(binding.textWord.text.toString())
        }
    }
}