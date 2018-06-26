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

package app.tivi.extensions

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.functions.Functions

fun <T> Maybe<T>.emptySubscribe() = subscribe(Functions.emptyConsumer(), Functions.ERROR_CONSUMER)
fun <T> Single<T>.emptySubscribe() = subscribe(Functions.emptyConsumer(), Functions.ERROR_CONSUMER)
fun <T> Flowable<T>.emptySubscribe() = subscribe(Functions.emptyConsumer(), Functions.ERROR_CONSUMER)
fun <T> Observable<T>.emptySubscribe() = subscribe(Functions.emptyConsumer(), Functions.ERROR_CONSUMER)
fun Completable.emptySubscribe() = subscribe(Functions.EMPTY_ACTION, Functions.ERROR_CONSUMER)

fun <T> Observable<T>.toFlowable() = toFlowable(BackpressureStrategy.LATEST)