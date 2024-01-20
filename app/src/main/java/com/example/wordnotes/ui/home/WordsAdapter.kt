package com.example.wordnotes.ui.home

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.wordnotes.R
import com.example.wordnotes.databinding.WordItemBinding
import com.example.wordnotes.utils.themeColor
import com.example.wordnotes.utils.timeAgo

class WordsAdapter(
    private var words: List<WordItem> = emptyList(),
    private val onItemClicked: (String) -> Unit,
    private val onItemLongClicked: (String) -> Boolean
) : Adapter<WordsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WordsViewHolder(WordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = words.size

    override fun onBindViewHolder(holder: WordsViewHolder, position: Int) {
        holder.bind(words[position], onItemClicked, onItemLongClicked)
    }

    /**
     * This implementation is only intended to explain how DiffUtil.Callback.getChangePayload() method works.
     * Without it, the logic code still works properly.
     */
    override fun onBindViewHolder(holder: WordsViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        else holder.bind(words[position].copy(isSelected = payloads[0] as Boolean), onItemClicked, onItemLongClicked)
    }

    override fun onViewRecycled(holder: WordsViewHolder) {
        holder.cancelAvatarFlipAnimation()
    }

    fun setData(words: List<WordItem>) {
        val diffResult = DiffUtil.calculateDiff(WordDiffUtilCallback(this.words, words))
        diffResult.dispatchUpdatesTo(this)
        this.words = words
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
        val word = wordItem.word
        binding.apply {
            if (wordItem.isSelected && !isShowingBack) {
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

            textAvatar.text = word.word[0].uppercase()
            textWord.text = word.word
            textIpa.text = word.ipa
            textTimestamp.text = timeAgo(root.context, word.timestamp)
            textMeaning.text = word.meaning
            imageRemind.visibility = if (word.isRemind) View.VISIBLE else View.GONE
            root.apply {
                setBackgroundColor(
                    if (wordItem.isSelected) context.themeColor(R.attr.color_selected_item_background)
                    else context.themeColor(com.google.android.material.R.attr.colorSurface)
                )
                setOnClickListener { onItemClicked(word.id) }
                setOnLongClickListener { onItemLongClicked(word.id) }
            }

            textIpa.visibility = if (word.ipa.isNotEmpty()) View.VISIBLE else View.GONE
            textMeaning.visibility = if (word.meaning.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }
}

class WordDiffUtilCallback(private val oldList: List<WordItem>, private val newList: List<WordItem>) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldPosition: Int, newPosition: Int) = oldList[oldPosition].word.id == newList[newPosition].word.id
    override fun areContentsTheSame(oldPosition: Int, newPosition: Int) = oldList[oldPosition] == newList[newPosition]
    override fun getChangePayload(oldPosition: Int, newPosition: Int) = newList[newPosition].isSelected
}