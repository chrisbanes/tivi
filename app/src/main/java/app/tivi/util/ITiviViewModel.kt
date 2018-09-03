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

package app.tivi.util

import app.tivi.interactors.Interactor
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

interface ITiviViewModel {
    val viewModelJob: Job
    val disposables: CompositeDisposable

    fun launchWithParent(
        context: CoroutineContext = DefaultDispatcher,
        block: suspend CoroutineScope.() -> Unit
    ) = launch(context = context, parent = viewModelJob, block = block)

    fun <P> launchInteractor(interactor: Interactor<P>, param: P): Job {
        return launch(context = interactor.dispatcher, parent = viewModelJob, block = { interactor(param) })
    }

    fun launchInteractor(interactor: Interactor<Unit>): Job {
        return launch(context = interactor.dispatcher, parent = viewModelJob, block = { interactor(Unit) })
    }
}