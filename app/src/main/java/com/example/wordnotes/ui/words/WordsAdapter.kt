package com.example.wordnotes.ui.words

import android.os.Build.VERSION_CODES.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.wordnotes.R
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.databinding.WordItemBinding

class WordsAdapter(words: List<Word> = emptyList(), private val onItemClicked: (String) -> Unit) : Adapter<WordsViewHolder>() {
    private val words: MutableList<Word> = words.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WordsViewHolder(WordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = words.size

    override fun onBindViewHolder(holder: WordsViewHolder, position: Int) {
        holder.bind(words[position], onItemClicked)
    }

    fun setData(words: List<Word>) {
        val diffResult = DiffUtil.calculateDiff(WordDiffUtilCallback(this.words, words))
        diffResult.dispatchUpdatesTo(this)
        this.words.apply {
            clear()
            addAll(words)
        }
    }
}

class WordsViewHolder(private val binding: WordItemBinding) : ViewHolder(binding.root) {
    fun bind(word: Word, onItemClicked: (String) -> Unit) {
        binding.apply {
            textAvatar.text = word.word[0].toString()
            textWord.text = word.word
            textIpa.text = word.ipa
            textTimestamp.text = "2m"
            textMeaning.text = word.meaning
            imageStar.setImageDrawable(
                if (word.isLearning) ContextCompat.getDrawable(binding.root.context, R.drawable.star_fill)
                else ContextCompat.getDrawable(binding.root.context, R.drawable.star)
            )

            root.setOnClickListener { onItemClicked(word.id) }
        }
    }
}

class WordDiffUtilCallback(private val oldList: List<Word>, private val newList: List<Word>) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].id == newList[newItemPosition].id
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]
}