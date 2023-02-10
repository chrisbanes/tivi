/*
 * Copyright 2022 Google LLC
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

package app.tivi.inject

import android.app.Activity
import androidx.core.os.ConfigurationCompat
import app.tivi.datetime.DateTimeFormatters
import java.util.Locale
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

@Component
abstract class ActivityModule(
    @get:Provides val activity: Activity,
) {
    @Provides
    fun provideMediumDateFormatter(
        locale: Locale,
    ): DateTimeFormatters {
        // TODO: make these lazily instantiated
        return DateTimeFormatters(
            mediumDate = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale),
            mediumDateTime = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale),
            shortDate = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(locale),
            shortTime = DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(locale),
        )
    }

    @Provides
    fun provideActivityLocale(activity: Activity): Locale {
        return ConfigurationCompat.getLocales(activity.resources.configuration).get(0)
            ?: Locale.getDefault()
    }
}
