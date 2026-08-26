package com.qust.helper.next.ui.component.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.qust.helper.next.utils.DateUtils
import com.qust.helper.next.utils.DateUtils.toLocalDate
import com.qust.helper.next.ui.component.ConfirmDialog
import com.qust.helper.next.ui.theme.Theme
import java.util.Calendar
import java.util.Date

/**
 * 年月日选择 Dialog
 */
@Composable
fun DatePickerDialog(
    visible: Boolean,
    uiState: DatePickerUIState,
    onDismiss: () -> Unit,
    onSelectDone: (String) -> Unit
) {
    ConfirmDialog(
        visible = visible,
        title = "选择日期",
        onDismiss = onDismiss,
        onConfirm = {
            uiState.getValue()
            onSelectDone(DateUtils.YMD.format(uiState.calendar.toLocalDate()))
            onDismiss()
        }
    ) {
        DatePicker(
            uiState = uiState,
            modifier = Modifier.wrapContentWidth(),
            style = Theme.textStyles.body.copy(color = Color.Black)
        )
    }
}

/**
 * 年月日选择组件
 */
@Composable
fun DatePicker(
    uiState: DatePickerUIState,
    modifier: Modifier = Modifier,
    style: TextStyle = Theme.textStyles.body
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        DatePicker(
            title = "年",
            value = uiState.year,
            range = uiState.yearRange,
            style = style,
            width = 88.dp,
            valueChange = uiState::setYearValue
        )

        DatePicker(
            title = "月",
            value = uiState.month,
            range = 1..12,
            style = style,
            valueChange = uiState::setMonthValue
        )

        DatePicker(
            title = "日",
            value = uiState.day,
            range = 1..uiState.endDay,
            style = style,
            valueChange = uiState::setDayValue
        )
    }
}

class DatePickerUIState(year: Int = -1, month: Int = -1, day: Int = -1) {

    val calendar: Calendar = Calendar.getInstance().also {
        val initialYear = if (year == -1) it.get(Calendar.YEAR) else year
        val initialMonth = if (month == -1) it.get(Calendar.MONTH) + 1 else month.coerceIn(1, 12)
        val initialDay = if (day == -1) it.get(Calendar.DAY_OF_MONTH) else day

        it.set(Calendar.DAY_OF_MONTH, 1)
        it.set(Calendar.YEAR, initialYear)
        it.set(Calendar.MONTH, initialMonth - 1)
        it.set(
            Calendar.DAY_OF_MONTH,
            initialDay.coerceIn(1, it.getActualMaximum(Calendar.DAY_OF_MONTH))
        )
    }

    val yearRange: IntRange = calendar.get(Calendar.YEAR).let { (it - 10)..(it + 10) }

    var year by mutableIntStateOf(calendar.get(Calendar.YEAR))
        private set
    var month by mutableIntStateOf(calendar.get(Calendar.MONTH) + 1)
        private set
    var day by mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH))
        private set
    var endDay by mutableIntStateOf(calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        private set

    fun setYearValue(year: Int) {
        this.year = year
        updateCalendarAndEndDay()
    }

    fun setMonthValue(month: Int) {
        this.month = month
        updateCalendarAndEndDay()
    }

    fun setDayValue(day: Int) {
        this.day = day.coerceIn(1, endDay)
        calendar.set(Calendar.DAY_OF_MONTH, this.day)
    }

    private fun updateCalendarAndEndDay() {
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        endDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        setDayValue(day)
    }

    /**
     * 获取选择的日期
     */
    fun getValue(): Date {
        calendar.set(year, month - 1, day, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
