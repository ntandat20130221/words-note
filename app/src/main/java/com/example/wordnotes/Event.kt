package com.example.wordnotes

import androidx.lifecycle.Observer

class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    fun getContentIfHasNotBeenHandled(): T? = if (hasBeenHandled) null else content.also { hasBeenHandled = true }
}

class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>) {
        event.getContentIfHasNotBeenHandled()?.let {
            onEventUnhandledContent(it)
        }
    }
}