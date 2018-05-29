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

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.core.content.systemService
import com.cantrowitz.rxbroadcast.RxBroadcast
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class AndroidNetworkDetector @Inject constructor(
    private val context: Context
) : NetworkDetector {
    private val connectivityManager = context.systemService<ConnectivityManager>()

    companion object {
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }

    private fun getConnectivityStatus(): Boolean {
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }

    override fun observe(): Observable<Boolean> {
        return RxBroadcast.fromBroadcast(context, intentFilter)
                .startWith(Intent())
                .map { getConnectivityStatus() }
                .distinctUntilChanged()
    }

    override fun waitForConnection(): Single<Boolean> {
        return observe()
                .filter { it }
                .firstOrError()
    }
}