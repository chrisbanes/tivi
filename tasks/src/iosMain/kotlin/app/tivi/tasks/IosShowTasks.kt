// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import app.tivi.domain.interactors.UpdateLibraryShows
import app.tivi.util.Logger
import kotlin.time.Duration.Companion.hours
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toNSDate
import me.tatarka.inject.annotations.Inject
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarMatchStrictly
import platform.Foundation.NSDate

@Inject
class IosShowTasks(
  private val updateLibraryShows: Lazy<UpdateLibraryShows>,
  private val logger: Logger,
) : ShowTasks {
  private val taskScheduler by lazy { BGTaskScheduler.sharedScheduler }
  private val scope by lazy { MainScope() + CoroutineName("app.tivi.tasks.IosShowTasks") }

  override fun register() {
    taskScheduler.registerForTaskWithIdentifier(
      identifier = ID_LIBRARY_SHOWS_NIGHTLY,
      usingQueue = null,
      launchHandler = { task -> handleTask(task!!) },
    )
    logger.d { "Registered task [$ID_LIBRARY_SHOWS_NIGHTLY] with BGTaskScheduler" }

    // Now schedule the next nightly sync
    scheduleTask(id = ID_LIBRARY_SHOWS_NIGHTLY, earliest = nextEarliestNightlySyncDate())
  }

  private fun handleTask(task: BGTask) = when (task.identifier) {
    ID_LIBRARY_SHOWS_NIGHTLY -> {
      task.runInteractor {
        updateLibraryShows.value(UpdateLibraryShows.Params(true))
      }
      // Now schedule another task
      scheduleTask(
        id = ID_LIBRARY_SHOWS_NIGHTLY,
        earliest = (Clock.System.now() + 22.hours).toNSDate(),
      )
    }

    else -> Unit
  }

  @OptIn(ExperimentalForeignApi::class)
  private fun scheduleTask(
    id: String,
    earliest: NSDate,
    requireNetwork: Boolean = true,
  ) {
    val request = BGProcessingTaskRequest(identifier = id).apply {
      earliestBeginDate = earliest
      requiresNetworkConnectivity = requireNetwork
    }

    try {
      BGTaskScheduler.sharedScheduler.submitTaskRequest(taskRequest = request, error = null)
      logger.d { "Scheduled task $id. Earliest date: $earliest" }
    } catch (t: Throwable) {
      logger.e(t) { "Error whilst submitting BGTaskScheduler request: $request" }
    }
  }

  private fun BGTask.runInteractor(block: suspend () -> Unit) {
    logger.d { "Starting to run task [$identifier]" }

    val job = scope.launch { block() }

    expirationHandler = {
      logger.d { "Expiration handler called for task [$identifier]" }
      setTaskCompletedWithSuccess(false)
      job.cancel()
    }

    runBlocking {
      try {
        job.join()
        setTaskCompletedWithSuccess(true)
        logger.d { "Task [$identifier] finished successfully" }
      } catch (e: Throwable) {
        setTaskCompletedWithSuccess(false)
        logger.d(e) { "Exception thrown whilst running task [$identifier]" }
      }
    }
  }

  companion object {
    const val ID_LIBRARY_SHOWS_NIGHTLY = "app.tivi.tasks.libraryshows.nightly"
  }
}

/**
 * Returns the next date at 03:00
 */
private fun nextEarliestNightlySyncDate(): NSDate = NSCalendar.currentCalendar().nextDateAfterDate(
  date = NSDate(),
  matchingHour = 3,
  minute = 0,
  second = 0,
  options = NSCalendarMatchStrictly,
)!!
