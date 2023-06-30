// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.runtime

import kotlin.reflect.KClass

/**
 * A [CircuitContext] is used in `Presenter.Factory` and `Ui.Factory` to inform creation of their
 * respective `Presenter` and `Ui` instances.
 *
 * Available information includes:
 * - [parent] – the parent [CircuitContext] or null if this is the root context.
 * - [tag] – a tag API to plumb arbitrary metadata through the [CircuitContext].
 */
public class CircuitContext
@InternalCircuitApi
public constructor(
  public val parent: CircuitContext?,
) {
  // Don't expose the raw map.
  private val tags = mutableMapOf<KClass<*>, Any>()

  /** Returns the tag attached with [type] as a key, or null if no tag is attached with that key. */
  public fun <T : Any> tag(type: KClass<T>): T? {
    @Suppress("UNCHECKED_CAST") return tags[type] as T?
  }

  /** Returns the tag attached with [T] as a key, or null if no tag is attached with that key. */
  public inline fun <reified T : Any> tag(): T? = tag(T::class)

  /**
   * Attaches [tag] to the request using [T] as a key. Tags can be read from a request using
   * [CircuitContext.tag]. Use `null` to remove any existing tag assigned for [T].
   *
   * Use this API to attach metadata, debugging, or other application data to a spec so that you may
   * read it in other APIs or callbacks.
   */
  public inline fun <reified T : Any> putTag(tag: T?): Unit = putTag(T::class, tag)

  /**
   * Attaches [tag] to the request using [type] as a key. Tags can be read from a request using
   * [CircuitContext.tag]. Use `null` to remove any existing tag assigned for [type].
   *
   * Use this API to attach metadata, debugging, or other application data to a spec so that you may
   * read it in other APIs or callbacks.
   */
  public fun <S : Any, T : S> putTag(type: KClass<S>, tag: T?): Unit {
    if (tag == null) {
      this.tags.remove(type)
    } else {
      this.tags[type] = tag
    }
  }

  /** Clears all the current tags. */
  public fun clearTags() {
    tags.clear()
  }
}
