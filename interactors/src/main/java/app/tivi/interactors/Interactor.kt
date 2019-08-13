/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.interactors

import androidx.paging.PagedList
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext

interface Interactor<in P> {
    val dispatcher: CoroutineDispatcher
    suspend operator fun invoke(params: P)
}

interface ObservableInteractor<in P, T> : Interactor<P> {
    fun observe(): Flow<T>
}

abstract class PagingInteractor<P : PagingInteractor.Parameters<T>, T> : SubjectInteractor<P, PagedList<T>>() {
    interface Parameters<T> {
        val pagingConfig: PagedList.Config
        val boundaryCallback: PagedList.BoundaryCallback<T>?
    }
}

abstract class SuspendingWorkInteractor<P : Any, T : Any> : ObservableInteractor<P, T> {
    private val subject = BehaviorSubject.create<T>()
    private val flow = subject.toFlowable(BackpressureStrategy.LATEST).asFlow()

    override suspend operator fun invoke(params: P) = subject.onNext(doWork(params))

    abstract suspend fun doWork(params: P): T

    override fun observe(): Flow<T> = flow
}

abstract class SubjectInteractor<P : Any, T> : ObservableInteractor<P, T> {
    private val subject = BehaviorSubject.create<P>()
    private val flow = subject.toFlowable(BackpressureStrategy.LATEST)
            .asFlow()
            .distinctUntilChanged()
            .flatMapLatest { createObservable(it) }

    override suspend operator fun invoke(params: P) = subject.onNext(params)

    protected abstract fun createObservable(params: P): Flow<T>

    override fun observe(): Flow<T> = flow
}

fun <P> CoroutineScope.launchInteractor(
    interactor: Interactor<P>,
    param: P,
    f: (Boolean) -> Unit = {}
) = launch(context = interactor.dispatcher) {
    f(false)
    interactor(param)
    f(true)
}

suspend fun <P> Interactor<P>.execute(param: P) = withContext(context = dispatcher) {
    invoke(param)
}

fun CoroutineScope.launchInteractor(interactor: Interactor<Unit>) = launchInteractor(interactor, Unit)

fun <I : ObservableInteractor<*, T>, T> CoroutineScope.launchObserve(
    interactor: I,
    f: suspend (Flow<T>) -> Unit
) {
    launch(interactor.dispatcher) {
        f(interactor.observe())
    }
}