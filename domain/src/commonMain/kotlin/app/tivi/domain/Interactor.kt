// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.util.cancellableRunCatching
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeout

interface UserInitiatedParams {
  val isUserInitiated: Boolean
}

abstract class Interactor<in P, R> {
  private val loadingState = MutableStateFlow(State())

  @OptIn(FlowPreview::class)
  val inProgress: Flow<Boolean> by lazy {
    loadingState
      .debounce {
        if (it.ambientCount > 0) {
          5.seconds
        } else {
          0.seconds
        }
      }
      .map { (it.userCount + it.ambientCount) > 0 }
      .distinctUntilChanged()
  }

  private fun addLoader(fromUser: Boolean) {
    loadingState.update {
      if (fromUser) {
        it.copy(userCount = it.userCount + 1)
      } else {
        it.copy(ambientCount = it.ambientCount + 1)
      }
    }
  }

  private fun removeLoader(fromUser: Boolean) {
    loadingState.update {
      if (fromUser) {
        it.copy(userCount = it.userCount - 1)
      } else {
        it.copy(ambientCount = it.ambientCount - 1)
      }
    }
  }

  suspend operator fun invoke(
    params: P,
    timeout: Duration = DefaultTimeout,
    userInitiated: Boolean = params.isUserInitiated,
  ): Result<R> = cancellableRunCatching {
    addLoader(userInitiated)
    withTimeout(timeout) {
      doWork(params)
    }
  }.also {
    removeLoader(userInitiated)
  }

  private val P.isUserInitiated: Boolean
    get() = (this as? UserInitiatedParams)?.isUserInitiated ?: true

  protected abstract suspend fun doWork(params: P): R

  companion object {
    internal val DefaultTimeout = 5.minutes
  }

  private data class State(val userCount: Int = 0, val ambientCount: Int = 0)
}

suspend operator fun <R> Interactor<Unit, R>.invoke(
  timeout: Duration = Interactor.DefaultTimeout,
) = invoke(Unit, timeout)

abstract class PagingInteractor<P : PagingInteractor.Parameters<T>, T : Any> : SubjectInteractor<P, PagingData<T>>() {
  interface Parameters<T : Any> {
    val pagingConfig: PagingConfig
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
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
