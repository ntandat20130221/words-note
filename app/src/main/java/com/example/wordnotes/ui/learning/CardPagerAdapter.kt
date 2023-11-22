package com.example.wordnotes.ui.learning

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.wordnotes.data.model.Word
import kotlin.random.Random

class CardPagerAdapter(
    fragment: Fragment,
    private val type: Int,
) : FragmentStateAdapter(fragment) {
    private var okPositions: Set<Int> = emptySet()

    private val differ: AsyncListDiffer<Word> = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Word>() {
        override fun areItemsTheSame(oldItem: Word, newItem: Word) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Word, newItem: Word) = oldItem == newItem
    })

    override fun getItemCount(): Int = differ.currentList.size

    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment = when (type) {
            FlashCardFragment.TYPE_WORD -> TypeOneCardFragment()
            FlashCardFragment.TYPE_MEANING -> TypeTwoCardFragment()
            FlashCardFragment.TYPE_MIXED -> if (Random.nextInt() % 2 == 0) TypeOneCardFragment() else TypeTwoCardFragment()
            else -> TypeOneCardFragment()
        }

        return fragment.apply {
            arguments = bundleOf(
                KEY_WORD to differ.currentList[position],
                KEY_OK to okPositions.contains(position)
            )
        }
    }

    fun updateData(list: List<Word>, okPositions: Set<Int>, commitCallback: Runnable? = null) {
        differ.submitList(list, commitCallback)
        this.okPositions = okPositions
    }
}