package com.example.wordnotes.ui.home

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.wordnotes.R
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.databinding.WordItemBinding
import com.example.wordnotes.utils.themeColor
import com.example.wordnotes.utils.timeAgo

class WordsAdapter(
    private val onItemClicked: (String) -> Unit,
    private val onItemLongClicked: (String) -> Boolean
) : ListAdapter<WordItem, WordsViewHolder>(WordDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WordsViewHolder(WordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: WordsViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked, onItemLongClicked)
    }

    override fun onViewRecycled(holder: WordsViewHolder) {
        holder.cancelAvatarFlipAnimation()
    }
}

class WordsViewHolder(private val binding: WordItemBinding) : ViewHolder(binding.root) {
    private val flipRightAnimator: AnimatorSet = AnimatorSet()
    private val flipLeftAnimator: AnimatorSet = AnimatorSet()
    private var isShowingBack = false

    init {
        val rightOut = AnimatorInflater.loadAnimator(binding.root.context, R.animator.flip_right_out)
        val rightIn = AnimatorInflater.loadAnimator(binding.root.context, R.animator.flip_right_in)
        rightOut.setTarget(binding.textAvatar)
        rightIn.setTarget(binding.imageAvatar)
        flipRightAnimator.playTogether(rightOut, rightIn)

        val leftOut = AnimatorInflater.loadAnimator(binding.root.context, R.animator.flip_left_out)
        val leftIn = AnimatorInflater.loadAnimator(binding.root.context, R.animator.flip_left_in)
        leftOut.setTarget(binding.imageAvatar)
        leftIn.setTarget(binding.textAvatar)
        flipLeftAnimator.playTogether(leftOut, leftIn)
    }

    fun cancelAvatarFlipAnimation() {
        isShowingBack = false
    }

    fun bind(wordItem: WordItem, onItemClicked: (String) -> Unit, onItemLongClicked: (String) -> Boolean) {
        binding.apply {
            bindSelected(wordItem.isSelected)
            bindWord(wordItem.word)
            root.apply {
                setOnClickListener { onItemClicked(wordItem.word.id) }
                setOnLongClickListener { onItemLongClicked(wordItem.word.id) }
            }
        }
    }

    private fun bindWord(word: Word) {
        binding.apply {
            textAvatar.text = word.word[0].uppercase()
            textWord.text = word.word
            textIpa.text = word.ipa
            textIpa.visibility = if (word.ipa.isNotEmpty()) View.VISIBLE else View.GONE
            textMeaning.text = word.meaning
            textMeaning.visibility = if (word.meaning.isNotEmpty()) View.VISIBLE else View.GONE
            textTimestamp.text = timeAgo(root.context, word.timestamp)
            imageRemind.visibility = if (word.isRemind) View.VISIBLE else View.GONE
        }
    }

    private fun bindSelected(isSelected: Boolean) {
        if (isSelected && !isShowingBack) {
            flipRightAnimator.start()
            if (flipLeftAnimator.isRunning) {
                flipRightAnimator.currentPlayTime = flipRightAnimator.totalDuration - flipLeftAnimator.currentPlayTime
                flipLeftAnimator.cancel()
            }
            binding.textAvatar.setHasTransientState(true)
            binding.imageAvatar.setHasTransientState(true)
            isShowingBack = true
        } else if (isShowingBack) {
            flipLeftAnimator.start()
            if (flipRightAnimator.isRunning) {
                flipLeftAnimator.currentPlayTime = flipLeftAnimator.totalDuration - flipRightAnimator.currentPlayTime
                flipRightAnimator.cancel()
            }
            binding.textAvatar.setHasTransientState(true)
            binding.imageAvatar.setHasTransientState(true)
            isShowingBack = false
        }
        binding.root.apply {
            this.isSelected = isSelected
            setBackgroundColor(
                if (isSelected) context.themeColor(R.attr.color_selected_item_background)
                else context.themeColor(com.google.android.material.R.attr.colorSurface)
            )
        }
    }
}

private class WordDiffUtilCallback : DiffUtil.ItemCallback<WordItem>() {
    override fun areItemsTheSame(oldItem: WordItem, newItem: WordItem) = oldItem.word.id == newItem.word.id
    override fun areContentsTheSame(oldItem: WordItem, newItem: WordItem) = oldItem == newItem
}