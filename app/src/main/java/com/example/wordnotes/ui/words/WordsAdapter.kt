package com.example.wordnotes.ui.words

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.wordnotes.R
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.databinding.WordItemBinding

class WordsAdapter(private val words: List<Word>) : Adapter<WordsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WordsViewHolder(WordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = words.size

    override fun onBindViewHolder(holder: WordsViewHolder, position: Int) {
        holder.bind(words[position])
    }
}

class WordsViewHolder(private val binding: WordItemBinding) : ViewHolder(binding.root) {
    fun bind(word: Word) {
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
        }
    }
}