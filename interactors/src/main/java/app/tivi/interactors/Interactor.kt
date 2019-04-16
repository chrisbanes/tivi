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

import androidx.paging.DataSource
import app.tivi.extensions.toFlowable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.withContext

interface Interactor<in P> {
    val dispatcher: CoroutineDispatcher
    suspend operator fun invoke(executeParams: P)
}

interface PagingInteractor<T> {
    fun dataSourceFactory(): DataSource.Factory<Int, T>
}

abstract class ChannelInteractor<P, T : Any> : Interactor<P> {
    private val channel = Channel<T>()

    final override suspend fun invoke(executeParams: P) {
        channel.offer(execute(executeParams))
    }

    fun observe(): Flowable<T> = channel.asObservable(dispatcher).toFlowable()

    protected abstract suspend fun execute(executeParams: P): T

    fun clear() {
        channel.close()
    }
}

abstract class SubjectInteractor<P : Any, EP, T> : Interactor<EP> {
    private var disposable: Disposable? = null
    private val subject: BehaviorSubject<T> = BehaviorSubject.create()

    val loading = BehaviorSubject.createDefault(false)

    private lateinit var params: P

    fun setParams(params: P) {
        this.params = params
        setSource(createObservable(params))
    }

    final override suspend fun invoke(executeParams: EP) {
        loading.onNext(true)
        doWork(params, executeParams)
        loading.onNext(false)
    }

    protected abstract suspend fun doWork(params: P, executeParams: EP)

    protected abstract fun createObservable(params: P): Flowable<T>

    fun clear() {
        disposable?.dispose()
        disposable = null
    }

    fun observe(): Flowable<T> = subject.toFlowable()

    private fun setSource(source: Flowable<T>) {
        disposable?.dispose()
        disposable = source.subscribe(subject::onNext, subject::onError)
    }
}

fun <P> CoroutineScope.launchInteractor(interactor: Interactor<P>, param: P): Job {
    return launch(context = interactor.dispatcher, block = { interactor(param) })
}

suspend fun <P> Interactor<P>.execute(param: P) = withContext(context = dispatcher) {
    invoke(param)
}

fun CoroutineScope.launchInteractor(interactor: Interactor<Unit>) = launchInteractor(interactor, Unit)