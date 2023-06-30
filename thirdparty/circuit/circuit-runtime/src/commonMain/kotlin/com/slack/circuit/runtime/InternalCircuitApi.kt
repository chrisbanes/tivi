// Copyright (C) 2023 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.runtime

/**
 * Marks declarations that are **internal** in Circuit API, which means that should not be used
 * outside of `com.slack.circuit`, because their signatures and semantics will change between future
 * releases without any warnings and without providing any migration aids.
 */
@Retention(AnnotationRetention.BINARY)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.TYPEALIAS,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.CONSTRUCTOR,
)
@RequiresOptIn(
  level = RequiresOptIn.Level.ERROR,
  message =
    "This is an internal Circuit API that " +
      "should not be used from outside of com.slack.circuit. No compatibility guarantees are provided. " +
      "It is recommended to report your use-case of internal API to Circuit issue tracker, " +
      "so a stable API could be provided instead"
)
public annotation class InternalCircuitApi
