package com.example.wordnotes.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

sealed interface Result<out R> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
}

suspend fun <T> wrapWithResult(block: suspend () -> T) = try {
    Result.Success(block.invoke())
} catch (e: Exception) {
    Result.Error(e)
}

fun <T> Flow<T>.asResult(): Flow<Result<T>> = map<T, Result<T>> { Result.Success(it) }
    .catch { emit(Result.Error(it)) }