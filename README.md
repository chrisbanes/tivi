![Tivi](https://raw.githubusercontent.com/chrisbanes/tivi/master/art/banner.png)

# Tivi 📺 (work-in-progress 👷🔧️👷‍♀️⛏)

**This is not an official Google product**

Tivi is a **work-in-progress** TV show tracking Android app, which connects to
[Trakt.tv](https://www.trakt.tv). It is still in its early stages of development and currently
only contains two pieces of UI. It is under heavy development.

## Android development

Tivi is an app which attempts to use the latest cutting edge libraries and tools. As a summary:

 * Entirely written in [Kotlin](https://kotlinlang.org/)
 * Uses [RxJava](https://github.com/ReactiveX/RxJava) 2
 * Uses all of the [Architecture Components](https://developer.android.com/topic/libraries/architecture/): Room, LiveData and Lifecycle-components
 * Uses [dagger-android](https://google.github.io/dagger/android.html) for dependency injection

## Development setup

First off, you require the latest Android Studio 3.0 (or newer) to be able to build the app.

### API keys

You need to supply API / client keys for the various services the
app uses. That is currently [Trakt.tv](http://docs.trakt.apiary.io/) and
[TMDb](https://developers.themoviedb.org/4/getting-started). You can find information about
how to gain access via the relevant links.

When you obtain the keys, you can provide them to the app by putting the following in the
`gradle.properties` file in your user home:

```
# Get these from Trakt.tv
TIVI_TRAKT_CLIENT_ID=<insert>
TIVI_TRAKT_CLIENT_SECRET=<insert>

# Get this from TMDb
TIVI_TMDB_API_KEY=<insert>
```

On Linux/Mac that file is typically found at `~/.gradle/gradle.properties`.

## Contributions

If you've found an error in this sample, please file an issue.

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request. Since this project is still in its very early stages,
if your change is substantial, please raise an issue first to discuss it.

## License

```
Copyright 2017 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements. See the NOTICE file distributed with this work for
additional information regarding copyright ownership. The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
