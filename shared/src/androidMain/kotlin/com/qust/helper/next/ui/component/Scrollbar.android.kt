package com.qust.helper.next.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
actual fun ColumnVerticalScroll(
    modifier: Modifier,
    scrollBarSpace: Dp,
    content: @Composable (ColumnScope.() -> Unit)
) {
    val state = rememberScrollState()

    Row(modifier) {
        Column(Modifier.verticalScroll(state), content = content)
    }
}

@Composable
actual fun ColumnHorizontalScroll(modifier: Modifier, scrollBarSpace: Dp, content: @Composable ColumnScope.() -> Unit) {
    val state = rememberScrollState()

    Column(modifier) {
        Column(Modifier.horizontalScroll(state), content = content)
    }
}

@Composable
actual fun RowVerticalScroll(modifier: Modifier, scrollBarSpace: Dp, content: @Composable RowScope.() -> Unit) {
    val state = rememberScrollState()

    Row(modifier) {
        Row(Modifier.verticalScroll(state), content = content)
    }
}

@Composable
actual fun RowHorizontalScroll(modifier: Modifier, scrollBarSpace: Dp, content: @Composable RowScope.() -> Unit) {
    val state = rememberScrollState()

    Column(modifier) {
        Row(Modifier.horizontalScroll(state), content = content)
    }
}

@Composable
actual fun LazyColumnVerticalScroll(modifier: Modifier, scrollBarSpace: Dp, content: LazyListScope.() -> Unit) {
    val state = rememberLazyListState()

    Row(modifier) {
        LazyColumn(state = state, content = content)
    }
}

@Composable
actual fun LazyRowHorizontalScroll(modifier: Modifier, scrollBarSpace: Dp, content: LazyListScope.() -> Unit) {
    val state = rememberLazyListState()

    Column(modifier) {
        LazyRow(state = state, content = content)
    }
}

@Composable
actual fun LazyVerticalGridVerticalScroll(
    modifier: Modifier,
    columns: GridCells,
    scrollBarSpace: Dp,
    content: LazyGridScope.() -> Unit
) {
    val state = rememberLazyGridState()

    Row(modifier) {
        LazyVerticalGrid(columns = columns, state = state, content = content)
    }
}