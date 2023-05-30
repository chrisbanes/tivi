// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain

import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout

abstract class Interactor<in P, R> {
    private val count = AtomicInteger()
    private val loadingState = MutableStateFlow(count.get())

    val inProgress: Flow<Boolean>
        get() = loadingState.map { it > 0 }.distinctUntilChanged()

    private fun addLoader() {
        loadingState.value = count.incrementAndGet()
    }

    private fun removeLoader() {
        loadingState.value = count.decrementAndGet()
    }

    suspend operator fun invoke(
        params: P,
        timeoutMs: Long = DefaultTimeoutMs,
    ): Result<R> {
        return try {
            addLoader()
            runCatching {
                withTimeout(timeoutMs) {
                    doWork(params)
                }
            }
        } finally {
            removeLoader()
        }
    }

    protected abstract suspend fun doWork(params: P): R

    companion object {
        internal val DefaultTimeoutMs = TimeUnit.MINUTES.toMillis(5)
    }
}

suspend fun <R> Interactor<Unit, R>.invoke(
    timeoutMs: Long = Interactor.DefaultTimeoutMs,
) = invoke(Unit, timeoutMs)

abstract class PagingInteractor<P : PagingInteractor.Parameters<T>, T : Any> : SubjectInteractor<P, PagingData<T>>() {
    interface Parameters<T : Any> {
        val pagingConfig: PagingConfig
    }
}

abstract class SuspendingWorkInteractor<P : Any, T> : SubjectInteractor<P, T>() {
    override fun createObservable(params: P): Flow<T> = flow {
        emit(doWork(params))
    }

    abstract suspend fun doWork(params: P): T
}

abstract class SubjectInteractor<P : Any, T> {
    // Ideally this would be buffer = 0, since we use flatMapLatest below, BUT invoke is not
    // suspending. This means that we can't suspend while flatMapLatest cancels any
    // existing flows. The buffer of 1 means that we can use tryEmit() and buffer the value
    // instead, resulting in mostly the same result.
    private val paramState = MutableSharedFlow<P>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val flow: Flow<T> = paramState
        .distinctUntilChanged()
        .flatMapLatest { createObservable(it) }
        .distinctUntilChanged()

    operator fun invoke(params: P) {
        paramState.tryEmit(params)
    }

    protected abstract fun createObservable(params: P): Flow<T>
}
