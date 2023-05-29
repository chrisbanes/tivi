// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.appinitializers

import android.app.Application
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import me.tatarka.inject.annotations.Inject

@Inject
class EmojiInitializer(
    private val application: Application,
) : AppInitializer {
    override fun init() {
        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            app.tivi.common.ui.R.array.com_google_android_gms_fonts_certs,
        )

        val config = FontRequestEmojiCompatConfig(application, fontRequest)
            .setReplaceAll(true)

        EmojiCompat.init(config)
    }
}
