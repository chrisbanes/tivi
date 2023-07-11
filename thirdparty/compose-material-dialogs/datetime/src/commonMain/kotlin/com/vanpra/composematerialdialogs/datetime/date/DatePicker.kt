package com.vanpra.composematerialdialogs.datetime.date

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.vanpra.composematerialdialogs.datetime.util.WeekFields
import com.vanpra.composematerialdialogs.datetime.util.getFullLocalName
import com.vanpra.composematerialdialogs.datetime.util.getNarrowDisplayName
import com.vanpra.composematerialdialogs.datetime.util.getShortLocalName
import com.vanpra.composematerialdialogs.datetime.util.isLeapYear
import com.vanpra.composematerialdialogs.datetime.util.isSmallDevice
import com.vanpra.composematerialdialogs.datetime.util.plusDays
import com.vanpra.composematerialdialogs.datetime.util.testLength
import com.vanpra.composematerialdialogs.datetime.util.withDayOfMonth
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

/**
 * @brief A date picker body layout
 *
 * @param initialDate time to be shown to the user when the dialog is first shown.
 * Defaults to the current date if this is not set
 * @param yearRange the range of years the user should be allowed to pick from
 * @param onDateChange callback with a LocalDateTime object when the user completes their input
 * @param allowedDateValidator when this returns true the date will be selectable otherwise it won't be
 */
@Composable
fun DatePicker(
    initialDate: LocalDate = remember {
        Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    },
    title: String = "",
    colors: DatePickerColors = DatePickerDefaults.colors(),
    yearRange: IntRange = IntRange(1900, 2100),
    allowedDateValidator: (LocalDate) -> Boolean = { true },
    locale: Locale = Locale.current,
    onDateChange: (LocalDate) -> Unit = {},
) {
    val datePickerState = remember {
        DatePickerState(initialDate, colors, yearRange)
    }

    DatePickerImpl(
        title = title,
        state = datePickerState,
        allowedDateValidator = allowedDateValidator,
        locale = locale,
    )

    LaunchedEffect(datePickerState) {
        snapshotFlow { datePickerState.selected }
            .collect { onDateChange(it) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DatePickerImpl(
    title: String,
    state: DatePickerState,
    allowedDateValidator: (LocalDate) -> Boolean,
    locale: Locale,
) {
    val pagerState = rememberPagerState(
        initialPage = (state.selected.year - state.yearRange.first) * 12 + state.selected.monthNumber - 1,
    )
    val pageCount = (state.yearRange.last - state.yearRange.first + 1) * 12

    Column(Modifier.fillMaxWidth()) {
        CalendarHeader(title, state, locale)
        HorizontalPager(
            pageCount = pageCount,
            state = pagerState,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.height(336.dp),
        ) { page ->
            val viewDate = remember {
                LocalDate(
                    state.yearRange.first + page / 12,
                    page % 12 + 1,
                    1,
                )
            }

            Column {
                CalendarViewHeader(viewDate, state, pagerState, locale, pageCount)
                Box {
                    androidx.compose.animation.AnimatedVisibility(
                        state.yearPickerShowing,
                        modifier = Modifier
                            .zIndex(0.7f)
                            .clipToBounds(),
                        enter = slideInVertically(initialOffsetY = { -it }),
                        exit = slideOutVertically(targetOffsetY = { -it }),
                    ) {
                        YearPicker(viewDate, state, pagerState)
                    }

                    CalendarView(viewDate, state, locale, allowedDateValidator)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun YearPicker(
    viewDate: LocalDate,
    state: DatePickerState,
    pagerState: PagerState,
) {
    val gridState = rememberLazyGridState(viewDate.year - state.yearRange.first)
    val coroutineScope = rememberCoroutineScope()

    Surface {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
        ) {
            itemsIndexed(state.yearRange.toList()) { _, item ->
                val selected = remember { item == viewDate.year }
                YearPickerItem(year = item, selected = selected, colors = state.colors) {
                    if (!selected) {
                        coroutineScope.launch {
                            pagerState.scrollToPage(
                                pagerState.currentPage + (item - viewDate.year) * 12,
                            )
                        }
                    }
                    state.yearPickerShowing = false
                }
            }
        }
    }
}

@Composable
private fun YearPickerItem(
    year: Int,
    selected: Boolean,
    colors: DatePickerColors,
    onClick: () -> Unit,
) {
    Box(Modifier.size(88.dp, 52.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(72.dp, 36.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.dateBackgroundColor(selected).value)
                .clickable(
                    onClick = onClick,
                    interactionSource = MutableInteractionSource(),
                    indication = rememberRipple(bounded = false),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = colors.dateTextColor(selected, true).value,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalendarViewHeader(
    viewDate: LocalDate,
    state: DatePickerState,
    pagerState: PagerState,
    locale: Locale,
    pageCount: Int,
) {
    val coroutineScope = rememberCoroutineScope()
    val month = remember { viewDate.month.getFullLocalName(locale) }

    Box(
        Modifier
            .padding(top = 16.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
            .height(24.dp)
            .fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .clickable(onClick = { state.yearPickerShowing = !state.yearPickerShowing }),
        ) {
            Text(
                text = "$month ${viewDate.year}",
                modifier = Modifier
                    .paddingFromBaseline(top = 16.dp)
                    .wrapContentSize(Alignment.Center),
                style = MaterialTheme.typography.titleSmall,
                color = state.colors.calendarHeaderTextColor,
            )

            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Year Selector",
                    tint = state.colors.calendarHeaderTextColor,
                    modifier = Modifier.rotate(if (state.yearPickerShowing) 180F else 0F),
                )
            }
        }

        Row(
            Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous Month",
                modifier = Modifier
                    .testTag("dialog_date_prev_month")
                    .size(24.dp)
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = rememberRipple(bounded = false),
                    ) {
                        coroutineScope.launch {
                            if (pagerState.currentPage - 1 >= 0) {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                tint = state.colors.calendarHeaderTextColor,
            )

            Spacer(modifier = Modifier.width(24.dp))

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Next Month",
                modifier = Modifier
                    .testTag("dialog_date_next_month")
                    .size(24.dp)
                    .clickable(
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage + 1 < pageCount)
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        interactionSource = MutableInteractionSource(),
                        indication = rememberRipple(bounded = false),
                    ),
                tint = state.colors.calendarHeaderTextColor,
            )
        }
    }
}

@Composable
private fun CalendarView(
    viewDate: LocalDate,
    state: DatePickerState,
    locale: Locale,
    allowedDateValidator: (LocalDate) -> Boolean,
) {
    Column(
        Modifier
            .padding(start = 12.dp, end = 12.dp)
            .testTag("dialog_date_calendar"),
    ) {
        DayOfWeekHeader(state, locale)
        val calendarDatesData = remember { getDates(viewDate, locale) }
        val datesList = remember { IntRange(1, calendarDatesData.second).toList() }
        val possibleSelected = remember(state.selected) {
            viewDate.year == state.selected.year && viewDate.month == state.selected.month
        }

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(240.dp)) {
            for (x in 0 until calendarDatesData.first) {
                item { Box(Modifier.size(40.dp)) }
            }

            items(datesList) {
                val selected = remember(state.selected) {
                    possibleSelected && it == state.selected.dayOfMonth
                }
                val date = viewDate.withDayOfMonth(it)
                val enabled = allowedDateValidator(date)
                DateSelectionBox(
                    date = it,
                    selected = selected,
                    colors = state.colors,
                    enabled = enabled,
                ) {
                    state.selected = date
                }
            }
        }
    }
}

@Composable
private fun DateSelectionBox(
    date: Int,
    selected: Boolean,
    colors: DatePickerColors,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .testTag("dialog_date_selection_$date")
            .size(40.dp)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = MutableInteractionSource(),
                indication = rememberRipple(bounded = false),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.toString(),
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.dateBackgroundColor(selected).value)
                .wrapContentSize(Alignment.Center),
            style = MaterialTheme.typography.labelMedium,
            color = colors.dateTextColor(selected, enabled).value,
        )
    }
}

@Composable
private fun DayOfWeekHeader(state: DatePickerState, locale: Locale) {
    val dayHeaders = WeekFields.of(locale).firstDayOfWeek.let { firstDayOfWeek ->
        (0L until 7L).map {
            firstDayOfWeek.plusDays(it).getNarrowDisplayName(locale)
        }
    }

    Row(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        LazyVerticalGrid(columns = GridCells.Fixed(7)) {
            dayHeaders.forEach {
                item {
                    Box(Modifier.size(40.dp)) {
                        Text(
                            it,
                            modifier = Modifier
                                .alpha(0.8f)
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center),
                            style = MaterialTheme.typography.labelLarge,
                            color = state.colors.calendarHeaderTextColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(title: String, state: DatePickerState, locale: Locale) {
    val month = remember(state.selected) { state.selected.month.getShortLocalName(locale) }
    val day = remember(state.selected) { state.selected.dayOfWeek.getShortLocalName(locale) }

    Box(
        Modifier
            .background(state.colors.headerBackgroundColor)
            .fillMaxWidth(),
    ) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp)) {
            Text(
                text = title,
                modifier = Modifier.paddingFromBaseline(top = if (isSmallDevice()) 24.dp else 32.dp),
                color = state.colors.headerTextColor,
                style = MaterialTheme.typography.labelMedium,
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .paddingFromBaseline(top = if (isSmallDevice()) 0.dp else 64.dp),
            ) {
                Text(
                    text = "$day, $month ${state.selected.dayOfMonth}",
                    modifier = Modifier.align(Alignment.CenterStart),
                    color = state.colors.headerTextColor,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            Spacer(Modifier.height(if (isSmallDevice()) 8.dp else 16.dp))
        }
    }
}

private fun getDates(date: LocalDate, locale: Locale): Pair<Int, Int> {
    val numDays = date.month.testLength(date.year, date.isLeapYear)

    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek.isoDayNumber
    val firstDay = date.withDayOfMonth(1).dayOfWeek.isoDayNumber - firstDayOfWeek % 7

    return Pair(firstDay, numDays)
}
