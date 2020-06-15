![Tivi](/art/banner.png)

# Tivi 📺 (work-in-progress 👷🔧️👷‍♀️⛏)

**This is not an official Google product**

Tivi is a **work-in-progress** TV show tracking Android app, which connects to
[Trakt.tv](https://www.trakt.tv). It is still in its early stages of development and currently
only contains two pieces of UI. It is under heavy development.

## Android development

Tivi is an app which attempts to use the latest cutting edge libraries and tools. As a summary:

 * Entirely written in [Kotlin](https://kotlinlang.org/)
 * Uses [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines/coroutines-guide.html)
 * Uses many of the [Architecture Components](https://developer.android.com/topic/libraries/architecture/): Room, LiveData and Lifecycle, Navigation
 * Uses [dagger-android](https://google.github.io/dagger/android.html) for dependency injection
 * Slowly being migrated to use [Jetpack Compose](https://developer.android.com/jetpack/compose)

## Development setup

First off, you require the latest Android Studio 4.1 Canary to be able to build the app. This is due to the project implementing some pieces of UI in [Jetpack Compose](https://developer.android.com/jetpack/compose).

### Code style

This project uses [ktlint](https://github.com/pinterest/ktlint), provided via
the [spotless](https://github.com/diffplug/spotless) gradle plugin, and the bundled project IntelliJ codestyle.

If you find that one of your pull reviews does not pass the CI server check due to a code style conflict, you can
easily fix it by running: `./gradlew spotlessApply`, or running IntelliJ/Android Studio's code formatter.

### API keys

You need to supply API / client keys for the various services the
app uses. That is currently [Trakt.tv](http://docs.trakt.apiary.io/),
[TMDb](https://developers.themoviedb.org/4/getting-started) and [Fabric](https://fabric.io) (for Crashlytics). You can find information about
how to gain access via the relevant links.

For Trakt.tv, set the redirect uri to `app.tivi.debug://auth/oauth2callback` for debug build types, or `app.tivi://auth/oauth2callback` for release build types.

When you obtain the keys, you can provide them to the app by putting the following in the
`gradle.properties` file in your user home:

```
# Get these from Trakt.tv
TIVI_TRAKT_CLIENT_ID=<insert>
TIVI_TRAKT_CLIENT_SECRET=<insert>

# Get this from TMDb
TIVI_TMDB_API_KEY=<insert>
```

On Linux/Mac that file is typically found at `~/.gradle/gradle.properties` or in the project directory `tivi/gradle.properties`

## Contributions

If you've found an error in this sample, please file an issue.

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request. Since this project is still in its very early stages,
if your change is substantial, please raise an issue first to discuss it.

## License

```
Copyright 2020 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
