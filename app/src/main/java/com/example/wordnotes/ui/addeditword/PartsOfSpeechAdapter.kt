package com.example.wordnotes.ui.addeditword

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.wordnotes.R
import com.example.wordnotes.databinding.PosItemBinding

class PartsOfSpeechAdapter(
    private val data: Array<String> = emptyArray(),
    private val onItemClicked: (Int) -> Unit
) : Adapter<PartsOfSpeechViewHolder>() {
    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartsOfSpeechViewHolder =
        PartsOfSpeechViewHolder(PosItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: PartsOfSpeechViewHolder, position: Int) {
        holder.bind(data[position], selectedIndex, onItemClicked)
    }

    fun setSelectedIndex(index: Int) {
        val oldIndex = selectedIndex
        selectedIndex = index
        notifyItemChanged(selectedIndex)
        notifyItemChanged(oldIndex)
    }
}

class PartsOfSpeechViewHolder(private val binding: PosItemBinding) : ViewHolder(binding.root) {
    fun bind(item: String, selectedIndex: Int, onItemClicked: (Int) -> Unit) {
        binding.apply {
            textPos.text = item

            root.apply {
                background =
                    if (selectedIndex == adapterPosition) ContextCompat.getDrawable(context, R.drawable.bg_pos_item_selected)
                    else ContextCompat.getDrawable(context, R.drawable.bg_input)
                setOnClickListener { onItemClicked(adapterPosition) }
            }
        }
    }
}