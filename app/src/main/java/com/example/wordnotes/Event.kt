package com.example.wordnotes

import androidx.lifecycle.Observer

class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfHasNotBeenHandled(): T? = if (hasBeenHandled) null else content.also { hasBeenHandled = true }
}

class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(value: Event<T>) {
        value.getContentIfHasNotBeenHandled()?.let {
            onEventUnhandledContent(it)
        }
    }
}