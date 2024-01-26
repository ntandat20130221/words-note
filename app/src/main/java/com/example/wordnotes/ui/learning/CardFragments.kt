package com.example.wordnotes.ui.learning

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.customviews.dp
import com.example.wordnotes.R
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.databinding.TypeOneCardBinding
import com.example.wordnotes.databinding.TypeTwoCardBinding
import com.example.wordnotes.data.TextToSpeechService

const val KEY_WORD = "com.example.wordnotes.ui.learning.key_word"
const val KEY_OK = "com.example.wordnotes.ui.learning.key_ok"

interface OnOkActionListener {
    fun onOkClicked()
}

abstract class CardFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId), OnOkActionListener {
    protected var word: Word? = null
    protected var isOk: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        @Suppress("DEPRECATION")
        word = arguments?.getParcelable(KEY_WORD)
        isOk = arguments?.getBoolean(KEY_OK) ?: false
    }
}

class TypeOneCardFragment : CardFragment(R.layout.type_one_card) {
    private var _binding: TypeOneCardBinding? = null
    private val binding get() = _binding!!

    private val ttsService: TextToSpeechService by lazy { TextToSpeechService(requireContext(), lifecycleScope) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = TypeOneCardBinding.bind(view)
        populateData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateData() {
        word?.let {
            binding.textWord.text = it.word
            binding.textIpa.text = it.ipa
            binding.textAnswer.text = if (it.pos.isNotEmpty()) "(${it.pos}) ${it.meaning}" else it.meaning
            binding.textAnswer.visibility = if (isOk) View.VISIBLE else View.GONE
            binding.card.setOnClickListener { ttsService.speak(binding.textWord.text.toString()) }
        }
    }

    override fun onOkClicked() {
        binding.textAnswer.visibility = View.VISIBLE
    }
}

class TypeTwoCardFragment : CardFragment(R.layout.type_two_card) {
    private var _binding: TypeTwoCardBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = TypeTwoCardBinding.bind(view)
        populateData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateData() {
        word?.let {
            binding.textMeaning.text = it.meaning
        }
        if (isOk) {
            checkAnswer()
        }
    }

    override fun onOkClicked() {
        checkAnswer()
    }

    private fun checkAnswer() {
        showAnswerResult(binding.inputWord.text.toString() == word?.word)
        binding.inputWord.clearFocus()
    }

    private fun showAnswerResult(isSuccess: Boolean) {
        val resultColor = requireContext().getColor(if (isSuccess) R.color.success else R.color.failure)
        binding.card.strokeColor = resultColor
        binding.card.strokeWidth = 3.dp.toInt()
        binding.resultView.apply {
            val background = background as? GradientDrawable
            background?.setColor(resultColor)
            visibility = View.VISIBLE
        }
        binding.imageCheck.apply {
            setImageResource(if (isSuccess) R.drawable.ic_check else R.drawable.ic_close)
            backgroundTintList = ColorStateList.valueOf(resultColor)
            visibility = View.VISIBLE
        }
    }
}
