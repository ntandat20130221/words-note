package com.example.wordnotes.data

sealed interface Result<out R> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val exception: Throwable? = null) : Result<Nothing>
    object Loading : Result<Nothing>
}

inline fun <reified T> Result<T>.onSuccess(callback: (data: T) -> Unit) {
    if (this is Result.Success) {
        callback(data)
    }
}