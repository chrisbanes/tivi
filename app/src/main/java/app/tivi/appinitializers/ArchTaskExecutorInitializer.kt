/*
 * Copyright 2019 Google LLC
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

package app.tivi.appinitializers

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import java.util.concurrent.Executor
import javax.inject.Inject

class ArchTaskExecutorInitializer @Inject constructor(
    private val backgroundExecutor: Executor
) : AppInitializer {
    @SuppressLint("RestrictedApi")
    override fun init(application: Application) {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            @Volatile private var mainHandler: Handler? = null
            private val lock = Any()

            override fun executeOnDiskIO(runnable: Runnable) {
                backgroundExecutor.execute(runnable)
            }

            override fun postToMainThread(runnable: Runnable) {
                if (mainHandler == null) {
                    synchronized(lock) {
                        if (mainHandler == null) {
                            mainHandler = Handler(Looper.getMainLooper())
                        }
                    }
                }
                mainHandler?.post(runnable)
            }

            override fun isMainThread(): Boolean {
                return Looper.getMainLooper().thread === Thread.currentThread()
            }
        })
    }
}