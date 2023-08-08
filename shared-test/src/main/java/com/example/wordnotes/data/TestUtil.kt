package com.example.wordnotes.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> createEmptyCollector(scope: CoroutineScope, scheduler: TestCoroutineScheduler, flow: Flow<T>) {
    scope.launch(UnconfinedTestDispatcher(scheduler)) { flow.collect() }
}