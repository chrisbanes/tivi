// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.cstr
import kotlinx.coroutines.DisposableHandle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSSelectorFromString
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIButton
import platform.UIKit.UIButtonConfiguration
import platform.UIKit.UIColor
import platform.UIKit.UIControl
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIControlEvents
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UILayoutConstraintAxisVertical
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIStackView
import platform.UIKit.UIViewController
import platform.UIKit.sheetPresentationController
import platform.darwin.NSObject
import platform.objc.OBJC_ASSOCIATION_RETAIN
import platform.objc.objc_removeAssociatedObjects
import platform.objc.objc_setAssociatedObject

@Composable
actual fun TimePickerDialog(
  onDismissRequest: () -> Unit,
  onTimeChanged: (LocalTime) -> Unit,
  selectedTime: LocalTime,
  confirmLabel: String,
  title: String,
) {
  DatePickerViewController(
    selectedDate = NSCalendar.currentCalendar().dateBySettingHour(
      h = selectedTime.hour.toLong(),
      minute = selectedTime.minute.toLong(),
      second = 0,
      ofDate = NSDate(),
      options = 0u,
    )!!,
    confirmLabel = confirmLabel,
    onDateChanged = { date ->
      onTimeChanged(
        date.toKotlinInstant()
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .time,
      )
    },
    datePickerCustomizer = {
      setPreferredDatePickerStyle(UIDatePickerStyle.UIDatePickerStyleWheels)
      datePickerMode = UIDatePickerMode.UIDatePickerModeTime
      maximumDate = NSDate() // has to be in past
    },
    onDismissRequest = onDismissRequest,
  )
}

@Composable
actual fun DatePickerDialog(
  onDismissRequest: () -> Unit,
  onDateChanged: (LocalDate) -> Unit,
  selectedDate: LocalDate,
  confirmLabel: String,
  minimumDate: LocalDate?,
  maximumDate: LocalDate?,
  title: String,
) {
  DatePickerViewController(
    selectedDate = NSCalendar.currentCalendar().dateFromComponents(
      LocalDateTime(selectedDate, midday).toNSDateComponents(),
    )!!,
    confirmLabel = confirmLabel,
    onDateChanged = { date ->
      onDateChanged(
        date.toKotlinInstant()
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date,
      )
    },
    datePickerCustomizer = {
      setPreferredDatePickerStyle(UIDatePickerStyle.UIDatePickerStyleInline)
      datePickerMode = UIDatePickerMode.UIDatePickerModeDate

      val cal = NSCalendar.currentCalendar()
      this.minimumDate = minimumDate?.toNSDateComponents()?.let { comps ->
        cal.dateFromComponents(comps)
      }
      this.maximumDate = maximumDate?.toNSDateComponents()?.let { comps ->
        cal.dateFromComponents(comps)
      }
    },
    onDismissRequest = onDismissRequest,
  )
}

@Composable
internal fun DatePickerViewController(
  confirmLabel: String,
  selectedDate: NSDate,
  onDateChanged: (NSDate) -> Unit,
  onDismissRequest: () -> Unit,
  datePickerCustomizer: UIDatePicker.() -> Unit = {},
  backgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
) {
  val viewController = LocalUIViewController.current

  val lastOnDateChanged by rememberUpdatedState(onDateChanged)
  val lastOnDismissRequest by rememberUpdatedState(onDismissRequest)

  val datePickerViewController = remember {
    DatePickerViewController(backgroundColor).apply {
      datePickerCustomizer(datePicker)

      confirmButton.setTitle(confirmLabel, UIControlStateNormal)
    }
  }

  DisposableEffect(datePickerViewController) {
    val handle = datePickerViewController.datePicker
      .addEventHandler(UIControlEventValueChanged) {
        lastOnDateChanged(date)
      }
    onDispose {
      handle.dispose()
    }
  }

  DisposableEffect(datePickerViewController) {
    val handle = datePickerViewController.confirmButton
      .addEventHandler(UIControlEventTouchUpInside) {
        lastOnDismissRequest()
      }
    onDispose {
      handle.dispose()
    }
  }

  LaunchedEffect(datePickerViewController, selectedDate) {
    datePickerViewController.datePicker.setDate(selectedDate)
  }

  DisposableEffect(viewController, datePickerViewController) {
    datePickerViewController.sheetPresentationController?.apply {
      detents = listOf(UISheetPresentationControllerDetent.mediumDetent())
    }

    viewController.presentViewController(datePickerViewController, true, null)

    datePickerViewController.onViewDisappeared = { lastOnDismissRequest() }

    onDispose {
      datePickerViewController.dismissViewControllerAnimated(true) {
        lastOnDismissRequest()
      }
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
private class DatePickerViewController(
  private val backgroundColor: Color,
) : UIViewController(nibName = null, bundle = null) {
  val datePicker = UIDatePicker().apply {
    translatesAutoresizingMaskIntoConstraints = false
  }
  val confirmButton = UIButton().apply {
    configuration = UIButtonConfiguration.borderlessButtonConfiguration()
    translatesAutoresizingMaskIntoConstraints = false
  }

  val stack = UIStackView().apply {
    axis = UILayoutConstraintAxisVertical
    spacing = 16.0
    layoutMarginsRelativeArrangement = true
    layoutMargins = UIEdgeInsetsMake(24.0, 24.0, 24.0, 24.0)
    translatesAutoresizingMaskIntoConstraints = false
  }

  var onViewDisappeared: () -> Unit = {}

  override fun viewDidLoad() {
    super.viewDidLoad()

    view.backgroundColor = UIColor(
      red = backgroundColor.red.toDouble(),
      green = backgroundColor.green.toDouble(),
      blue = backgroundColor.blue.toDouble(),
      alpha = backgroundColor.alpha.toDouble(),
    )

    view.addSubview(stack)

    NSLayoutConstraint.activateConstraints(
      listOf(
        stack.topAnchor.constraintEqualToAnchor(view.topAnchor),
        stack.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor),
        stack.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
      ),
    )

    stack.insertArrangedSubview(datePicker, 0u)
    stack.insertArrangedSubview(confirmButton, 1u)
  }

  override fun viewDidDisappear(animated: Boolean) {
    super.viewDidDisappear(animated)
    onViewDisappeared()
  }
}

private val midday by lazy { LocalTime(12, 0, 0, 0) }

@OptIn(ExperimentalForeignApi::class)
fun <T : UIControl> T.addEventHandler(
  event: UIControlEvents,
  lambda: T.() -> Unit,
): DisposableHandle {
  val lambdaTarget = ControlLambdaTarget(lambda)
  val action = NSSelectorFromString("action:")

  addTarget(
    target = lambdaTarget,
    action = action,
    forControlEvents = event,
  )

  objc_setAssociatedObject(
    `object` = this,
    key = "event$event".cstr,
    value = lambdaTarget,
    policy = OBJC_ASSOCIATION_RETAIN,
  )

  return DisposableHandle {
    removeTarget(target = lambdaTarget, action = action, forControlEvents = event)
    objc_removeAssociatedObjects(this@addEventHandler)
  }
}

@OptIn(BetaInteropApi::class)
@ExportObjCClass
private class ControlLambdaTarget<T : UIControl>(
  private val lambda: T.() -> Unit,
) : NSObject() {
  @ObjCAction
  fun action(sender: UIControl) {
    @Suppress("UNCHECKED_CAST")
    lambda(sender as T)
  }
}
