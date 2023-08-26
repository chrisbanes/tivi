// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.circuit

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.doOnDestroy

internal fun InstanceKeeper.child(key: String): InstanceKeeperDispatcher {
    val instance = getOrCreate(key) {
        ChildInstanceKeeperProvider().apply {
            // Remove the child provider from the parent keeper once the
            // child has been destroyed
            lifecycle.doOnDestroy {
                remove(key)?.onDestroy()
            }
        }
    }
    return instance.instanceKeeper
}

private class ChildInstanceKeeperProvider : InstanceKeeper.Instance {
    val instanceKeeper = InstanceKeeperDispatcher()
    val lifecycle = LifecycleRegistry(Lifecycle.State.CREATED)

    override fun onDestroy() {
        instanceKeeper.destroy()
        lifecycle.destroy()
    }
}
