// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

abstract class TiviActivity : ComponentActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    open fun handleIntent(intent: Intent) {}

    override fun finishAfterTransition() {
        val resultData = Intent()
        val result = onPopulateResultIntent(resultData)
        setResult(result, resultData)

        super.finishAfterTransition()
    }

    open fun onPopulateResultIntent(intent: Intent): Int = Activity.RESULT_OK
}
