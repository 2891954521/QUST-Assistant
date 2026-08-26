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
import com.qust.helper.next.ui.component.ConfirmDialog
import com.qust.helper.next.ui.theme.Theme
import java.util.Calendar
import java.util.Date



/**
 * 年月选择 Dialog
 */
@Composable
fun YearMonthPickDialog(
    visible: Boolean,
    uiState: YearMonthPickerUIState,
    onDismiss: () -> Unit,
    onSelectDone: (String) -> Unit)
{
    ConfirmDialog(
        visible = visible,
        title = "选择年月",
        onDismiss = onDismiss,
        onConfirm = {
        val date = uiState.calendar
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val formatDate = String.format("%d-%02d", year, month)
        onSelectDone(formatDate)
        onDismiss()
    }) {
        YearMonthPicker(uiState, modifier = Modifier.wrapContentWidth(), style = Theme.textStyles.body.copy(color = Color.Black))
    }
}

/**
 * 年月选择组件
 */
@Composable
fun YearMonthPicker(
    uiState: YearMonthPickerUIState,
    modifier: Modifier = Modifier,
    style: TextStyle = Theme.textStyles.body
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

        DatePicker(
            title = "年",
            value = uiState.year,
            range = uiState.yearRange,
            style = style,
            width = 88.dp
        ) {
            uiState.setYearValue(it)
        }

        DatePicker(title = "月", value = uiState.month, range = 1..12, style = style) {
            uiState.setMonthValue(it)
        }
    }
}


class YearMonthPickerUIState(year: Int = -1, month: Int = -1) {

    val calendar: Calendar = Calendar.getInstance().also {
        if(year != -1) it.set(Calendar.YEAR, year)
        if(month != -1) it.set(Calendar.MONTH, month - 1)
    }

    val yearRange: IntRange = calendar.get(Calendar.YEAR).let { (it - 10) .. (it + 10) }

    var year by mutableIntStateOf(calendar.get(Calendar.YEAR))
    var month by mutableIntStateOf(calendar.get(Calendar.MONTH) + 1)


    fun setYearValue(year: Int) {
        calendar.set(Calendar.YEAR, year)
        this.year = year
    }

    fun setMonthValue(month: Int) {
        calendar.set(Calendar.MONTH, month - 1)
        this.month = month
    }

    /**
     * 获取选择的年月
     */
    fun getValue(): Date {
        calendar.set(year, month - 1, 1, 0, 0, 0)
        return calendar.time
    }
}