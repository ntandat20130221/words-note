package com.example.wordnotes.ui.words

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.wordnotes.R
import com.example.wordnotes.databinding.WordItemBinding
import com.example.wordnotes.utils.themeColor
import com.example.wordnotes.utils.timeAgo

class WordsAdapter(
    private val words: MutableList<WordUiState> = mutableListOf(),
    private val onItemClicked: (String) -> Unit,
    private val onItemLongClicked: (String) -> Boolean
) : Adapter<WordsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WordsViewHolder(WordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = words.size

    override fun onBindViewHolder(holder: WordsViewHolder, position: Int) {
        holder.bind(words[position], onItemClicked, onItemLongClicked)
    }

    override fun onBindViewHolder(holder: WordsViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        else holder.bind(words[position].copy(isSelected = payloads[0] as Boolean), onItemClicked, onItemLongClicked)
    }

    fun setData(words: List<WordUiState>) {
        val diffResult = DiffUtil.calculateDiff(WordDiffUtilCallback(this.words, words))
        diffResult.dispatchUpdatesTo(this)
        this.words.apply {
            clear()
            addAll(words)
        }
    }
}

class WordsViewHolder(private val binding: WordItemBinding) : ViewHolder(binding.root) {
    fun bind(wordUiState: WordUiState, onItemClicked: (String) -> Unit, onItemLongClicked: (String) -> Boolean) {
        binding.apply {
            viewSwitcher.apply {
                findViewById<TextView>(R.id.text_avatar).text = wordUiState.word[0].uppercase()
                displayedChild = if (wordUiState.isSelected) 1 else 0
            }
            textWord.text = wordUiState.word
            textIpa.text = wordUiState.ipa
            textTimestamp.text = timeAgo(root.context, wordUiState.timestamp)
            textMeaning.text = wordUiState.meaning
            imageStar.setImageDrawable(
                if (wordUiState.isLearning) ContextCompat.getDrawable(binding.root.context, R.drawable.star_fill)
                else ContextCompat.getDrawable(binding.root.context, R.drawable.star)
            )
            root.apply {
                setBackgroundColor(
                    if (wordUiState.isSelected) context.themeColor(R.attr.color_selected_item_background)
                    else context.themeColor(com.google.android.material.R.attr.colorSurface)
                )
                setOnClickListener { onItemClicked(wordUiState.id) }
                setOnLongClickListener { onItemLongClicked(wordUiState.id) }
            }
        }
    }
}

class WordDiffUtilCallback(private val oldList: List<WordUiState>, private val newList: List<WordUiState>) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldPosition: Int, newPosition: Int) = oldList[oldPosition].id == newList[newPosition].id
    override fun areContentsTheSame(oldPosition: Int, newPosition: Int) = oldList[oldPosition] == newList[newPosition]
    override fun getChangePayload(oldPosition: Int, newPosition: Int) = newList[newPosition].isSelected
}