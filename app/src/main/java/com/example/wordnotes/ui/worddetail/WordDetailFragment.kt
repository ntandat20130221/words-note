package com.example.wordnotes.ui.worddetail

import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.databinding.FragmentWordDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class WordDetailFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentWordDetailBinding? = null
    private val binding get() = _binding!!

    private val wordDetailViewModel: WordDetailViewModel by viewModels { WordViewModelFactory }
    private val args: WordDetailFragmentArgs by navArgs()
    private lateinit var wordId: String
    private var tts: TextToSpeech? = null

    override fun getTheme() = R.style.BottomSheetDialogStyle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWordDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wordDetailViewModel.initializeWithWordId(args.wordId.also { wordId = it })
        observeUiState()
        setActionListeners()
        setUpTextToSpeech()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts?.shutdown()
        tts = null
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

                        textIpa.visibility = if (uiState.ipa.isNotEmpty()) View.VISIBLE else View.GONE
                        textMeaning.visibility = if (uiState.meaning.isNotEmpty()) View.VISIBLE else View.GONE

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
            lifecycleScope.launch {
                do {
                    val result = tts?.speak(binding.textWord.text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
                    delay(200)
                } while (result != TextToSpeech.SUCCESS)
            }
        }
    }

    private fun setUpTextToSpeech() {
        tts = TextToSpeech(requireContext().applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }
}