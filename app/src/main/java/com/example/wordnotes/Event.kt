package com.example.wordnotes

import androidx.lifecycle.Observer

class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContent(): T? = content
    fun getContentIfHasNotBeenHandled(): T? = if (hasBeenHandled) null else content.also { hasBeenHandled = true }
}

class OneTimeEventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(value: Event<T>) {
        value.getContentIfHasNotBeenHandled()?.let {
            onEventUnhandledContent(it)
        }
    }
}

class EventObserver<T>(private val callback: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(value: Event<T>) {
        value.getContent()?.let { callback(it) }
    }
}