package com.example.wordnotes.utils

import android.widget.EditText

fun EditText.setTextAndMoveCursor(text: String) {
    setText(text)
    setSelection(text.length)
}