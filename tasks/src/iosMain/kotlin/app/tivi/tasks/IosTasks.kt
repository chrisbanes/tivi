// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import app.tivi.domain.interactors.ScheduleEpisodeNotifications
import app.tivi.domain.interactors.UpdateLibraryShows
import app.tivi.inject.ApplicationCoroutineScope
import app.tivi.util.Logger
import kotlin.time.Duration.Companion.hours
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toNSDate
import me.tatarka.inject.annotations.Inject
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarMatchStrictly
import platform.Foundation.NSDate

@Inject
class IosTasks(
  updateLibraryShows: Lazy<UpdateLibraryShows>,
  scheduleEpisodeNotifications: Lazy<ScheduleEpisodeNotifications>,
  private val logger: Logger,
  private val scope: ApplicationCoroutineScope,
) : Tasks {
  private val taskScheduler by lazy { BGTaskScheduler.sharedScheduler }

  private val updateLibraryShows by updateLibraryShows
  private val scheduleEpisodeNotifications by scheduleEpisodeNotifications

  override fun setup() {
    registerTask(ID_LIBRARY_SHOWS_NIGHTLY)
    registerTask(ID_SCHEDULE_EPISODE_NOTIFICATIONS)
  }

  override fun scheduleLibrarySync() {
    scheduleTask(
      id = ID_LIBRARY_SHOWS_NIGHTLY,
      type = TaskType.Refresh,
      earliest = nextEarliestNightlySyncDate(),
    )
  }

  override fun cancelLibrarySync() {
    taskScheduler.cancelTaskRequestWithIdentifier(ID_LIBRARY_SHOWS_NIGHTLY)
  }

  override fun scheduleEpisodeNotifications() {
    scheduleTask(
      id = ID_SCHEDULE_EPISODE_NOTIFICATIONS,
      type = TaskType.Refresh,
      earliest = (Clock.System.now() + SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL).toNSDate(),
    )

    // iOS has no concept of running tasks while the app is open, so we'll just run them
    // manually now
    scope.launch { runScheduleEpisodeNotifications() }
  }

  override fun cancelEpisodeNotifications() {
    taskScheduler.cancelTaskRequestWithIdentifier(ID_SCHEDULE_EPISODE_NOTIFICATIONS)
  }

  private fun registerTask(id: String) {
    taskScheduler.registerForTaskWithIdentifier(
      identifier = id,
      usingQueue = null,
      launchHandler = ::handleTask,
    )
    logger.d { "Registered task [$id] with BGTaskScheduler" }
  }

  private fun handleTask(task: BGTask?) = when (task?.identifier) {
    ID_LIBRARY_SHOWS_NIGHTLY -> {
      task.runInteractor {
        updateLibraryShows(UpdateLibraryShows.Params(true))
      }
      // Now schedule another task
      scheduleTask(ID_LIBRARY_SHOWS_NIGHTLY, TaskType.Refresh, nextEarliestNightlySyncDate())
    }

    ID_SCHEDULE_EPISODE_NOTIFICATIONS -> {
      task.runInteractor(::runScheduleEpisodeNotifications)
      scheduleTask(
        id = ID_SCHEDULE_EPISODE_NOTIFICATIONS,
        type = TaskType.Refresh,
        earliest = (Clock.System.now() + SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL).toNSDate(),
      )
    }

    else -> Unit
  }

  private suspend fun runScheduleEpisodeNotifications() {
    // We always schedule notifications for longer than the next task schedule, just in case
    // the task doesn't run on time
    scheduleEpisodeNotifications(
      ScheduleEpisodeNotifications.Params(SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL * 1.5),
    )
  }

  @OptIn(ExperimentalForeignApi::class)
  private fun scheduleTask(
    id: String,
    type: TaskType,
    earliest: NSDate,
    requireNetwork: Boolean = true,
  ) {
    val request = when (type) {
      TaskType.Processing -> {
        BGProcessingTaskRequest(identifier = id).apply {
          earliestBeginDate = earliest
          requiresNetworkConnectivity = requireNetwork
        }
      }

      TaskType.Refresh -> {
        BGAppRefreshTaskRequest(identifier = id).apply {
          earliestBeginDate = earliest
        }
      }
    }

    try {
      BGTaskScheduler.sharedScheduler.submitTaskRequest(taskRequest = request, error = null)
      logger.d { "Scheduled task $id. Earliest date: $earliest" }
    } catch (t: Throwable) {
      logger.e(t) { "Error whilst submitting BGTaskScheduler request: $request" }
    }
  }

  internal enum class TaskType {
    Processing,
    Refresh,
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

  private companion object {
    // Important that these values are kept in sync with the values in the Info.plist
    const val ID_LIBRARY_SHOWS_NIGHTLY = "app.tivi.tasks.libraryshows.nightly"
    const val ID_SCHEDULE_EPISODE_NOTIFICATIONS = "app.tivi.tasks.episode.notifications"

    val SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL = 6.hours
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
