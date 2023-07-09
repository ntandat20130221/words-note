package com.example.wordnotes.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wordnotes.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WordsBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_words_bottom_sheet, container, false)
    }

    override fun getTheme() = R.style.BottomSheetDialogStyle
}