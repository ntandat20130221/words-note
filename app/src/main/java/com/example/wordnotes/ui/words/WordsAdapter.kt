package com.example.wordnotes.ui.words

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.wordnotes.R
import com.example.wordnotes.databinding.WordItemBinding

class WordsAdapter(
    words: List<WordUiState> = emptyList(),
    private val onItemClicked: (String) -> Unit,
    private val onItemLongClicked: (String) -> Boolean
) : Adapter<WordsViewHolder>() {
    private val words: MutableList<WordUiState> = words.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WordsViewHolder(WordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = words.size

    override fun onBindViewHolder(holder: WordsViewHolder, position: Int) {
        holder.bind(words[position], onItemClicked, onItemLongClicked)
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
            textAvatar.text = wordUiState.word[0].toString()
            textWord.text = wordUiState.word
            textIpa.text = wordUiState.ipa
            textTimestamp.text = "2m"
            textMeaning.text = wordUiState.meaning
            imageStar.setImageDrawable(
                if (wordUiState.isLearning) ContextCompat.getDrawable(binding.root.context, R.drawable.star_fill)
                else ContextCompat.getDrawable(binding.root.context, R.drawable.star)
            )
            root.apply {
                if (wordUiState.isSelected) {
                    setBackgroundColor(context.getColor(R.color.purple_200))
                }
                setOnClickListener { onItemClicked(wordUiState.id) }
                setOnLongClickListener { onItemLongClicked(wordUiState.id) }
            }
        }
    }
}

class WordDiffUtilCallback(private val oldList: List<WordUiState>, private val newList: List<WordUiState>) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].id == newList[newItemPosition].id
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]
}