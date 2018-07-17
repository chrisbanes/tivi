/*
 * Copyright 2018 Google, Inc.
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

import app.tivi.extensions.toFlowable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

interface Interactor<in P> {
    val dispatcher: CoroutineDispatcher
    suspend operator fun invoke(param: P)
}

interface Interactor2<P, T> : Interactor<P> {
    fun observe(): Flowable<T>
    fun clear() = Unit
}

abstract class SubjectInteractor<P, T> : Interactor2<P, T> {
    private var disposable : Disposable? = null
    private val subject: BehaviorSubject<T> = BehaviorSubject.create()

    final override suspend fun invoke(param: P) {
        setSource(createObservable(param))
        execute(param)
    }

    protected abstract fun createObservable(param: P): Flowable<T>

    protected abstract suspend fun execute(param: P)

    override fun clear() {
        disposable?.dispose()
        disposable = null
    }

    final override fun observe(): Flowable<T> = subject.toFlowable()

    private fun setSource(source: Flowable<T>) {
        disposable?.dispose()
        disposable = source.subscribe(subject::onNext, subject::onError)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> emptyInteractor(): Interactor<T> = EmptyInteractor as Interactor<T>

internal object EmptyInteractor : Interactor<Unit> {
    override val dispatcher: CoroutineDispatcher
        get() = DefaultDispatcher

    override suspend fun invoke(param: Unit) = Unit
}

fun <P> launchInteractor(
    interactor: Interactor<P>,
    param: P,
    context: CoroutineContext = interactor.dispatcher,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    parent: Job? = null
) = launch(context = context, start = start, parent = parent, block = { interactor(param) })