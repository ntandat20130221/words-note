package com.example.wordnotes.ui.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.wordnotes.databinding.FragmentFlashCardBinding
import com.example.wordnotes.utils.setUpToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FlashCardFragment : Fragment() {
    private var _binding: FragmentFlashCardBinding? = null
    private val binding get() = _binding!!

    private val args: FlashCardFragmentArgs by navArgs()
    private val flashCardViewModel: FlashCardViewModel by viewModels()
    private lateinit var cardPagerAdapter: CardPagerAdapter
    private var originalSoftInputMode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalSoftInputMode = requireActivity().window.attributes.softInputMode
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFlashCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setUpViewListeners()
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        originalSoftInputMode?.let { requireActivity().window.setSoftInputMode(it) }
    }

    private fun setUpToolbar() {
        binding.toolbar.toolbar.apply {
            findNavController().setUpToolbar(this)
        }
    }

    private fun setUpViewListeners() {
        binding.viewPager2.apply {
            adapter = CardPagerAdapter(this@FlashCardFragment, args.type).also { cardPagerAdapter = it }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    flashCardViewModel.setCurrentPage(position)
                }
            })
            (getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        binding.buttonPrev.setOnClickListener {
            flashCardViewModel.moveToPrevious()
        }

        binding.buttonNext.setOnClickListener {
            flashCardViewModel.moveToNext()
        }

        binding.buttonOk.setOnClickListener {
            flashCardViewModel.onOkButtonClicked()
            val listener = childFragmentManager.findFragmentByTag("f${binding.viewPager2.currentItem}") as? OnOkActionListener
            listener?.onOkClicked()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flashCardViewModel.uiState.collect { uiState ->
                    cardPagerAdapter.updateData(uiState.words, uiState.okPositions)
                    binding.viewPager2.currentItem = uiState.cardPosition
                }
            }
        }
    }

    companion object {
        const val TYPE_WORD = 0
        const val TYPE_MEANING = 1
        const val TYPE_MIXED = 2
    }
}