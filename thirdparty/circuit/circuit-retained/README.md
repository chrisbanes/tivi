circuit-retained
================

This optional artifact contains an implementation of `rememberRetained` and `produceRetainedState`. This is
useful for cases where you want to retain non-parcelable state across configuration changes. This
comes at the cost of not participating in the `SavedStateRegistry` and thus not being able to
persist across process death.

The only meaningful implementation in this artifact is for Android, where a backing `ViewModel` is used
to hold values. If you're not running this on Android, you shouldn't need this artifact.

## Installation

Simply provide the `LocalRetainedStateRegistry` composition local using `continuityRetainedStateRegistry()`.

```kotlin
CompositionLocalProvider(
  LocalRetainedStateRegistry provides continuityRetainedStateRegistry(),
) {
  // Content
}
```
