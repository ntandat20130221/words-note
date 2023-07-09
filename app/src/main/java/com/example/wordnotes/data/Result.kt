package com.example.wordnotes.data

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

inline fun <reified T> Result<T>.onSuccess(callback: (data: T) -> Unit) {
    if (this is Result.Success) {
        callback(data)
    }
}

inline fun <reified T> Result<T>.onError(callback: (throwable: Throwable?) -> Unit) {
    if (this is Result.Error) {
        callback(exception)
    }
}

inline fun <reified T> Result<T>.onLoading(callback: () -> Unit) {
    if (this is Result.Loading) {
        callback()
    }
}