// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import app.tivi.inject.ApplicationScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import me.tatarka.inject.annotations.Inject
import net.openid.appauth.AuthState as AppAuthAuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication

@ApplicationScope
@Inject
class AndroidTraktLoginAction(
  private val application: Application,
  private val loginTraktActivityResultContract: Lazy<LoginTraktActivityResultContract>,
  private val clientAuth: Lazy<ClientAuthentication>,
  private val authService: Lazy<AuthorizationService>,
) : TraktLoginAction {
  private lateinit var launcher: ActivityResultLauncher<Unit>

  private val resultChannel = Channel<Result<AuthState>>()

  internal fun registerActivityWatcher() {
    val callback = object : ActivityLifecycleCallbacksAdapter() {
      val launcherActivities by lazy {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
          addCategory(Intent.CATEGORY_LAUNCHER)
        }
        application.packageManager.queryIntentActivities(launcherIntent, 0)
      }

      override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activity is ComponentActivity &&
          launcherActivities.any { it.activityInfo.name == activity::class.qualifiedName }
        ) {
          register(activity)
        }
      }
    }
    application.registerActivityLifecycleCallbacks(callback)
  }

  private fun register(activity: ComponentActivity) {
    launcher = activity.registerForActivityResult(
      loginTraktActivityResultContract.value,
    ) { result ->
      if (result == null) return@registerForActivityResult

      val (response, error) = result
      when {
        response != null -> {
          authService.value.performTokenRequest(
            response.createTokenExchangeRequest(),
            clientAuth.value,
          ) { tokenResponse, ex ->
            val state = AppAuthAuthState()
              .apply { update(tokenResponse, ex) }
              .let(::AppAuthAuthStateWrapper)

            resultChannel.trySendBlocking(Result.success(state))
          }
        }

        error != null -> {
          Logger.d(error) { "AuthException" }
          resultChannel.trySendBlocking(Result.failure(error))
        }
      }
    }
  }

  override suspend operator fun invoke(): AuthState? {
    launcher.launch(Unit)
    return resultChannel.receive().getOrNull()
  }
}

private open class ActivityLifecycleCallbacksAdapter : Application.ActivityLifecycleCallbacks {
  override fun onActivityCreated(activity: Activity, bundle: Bundle?) = Unit
  override fun onActivityStarted(activity: Activity) = Unit
  override fun onActivityResumed(activity: Activity) = Unit
  override fun onActivityPaused(activity: Activity) = Unit
  override fun onActivityStopped(activity: Activity) = Unit
  override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit
  override fun onActivityDestroyed(activity: Activity) = Unit
}
