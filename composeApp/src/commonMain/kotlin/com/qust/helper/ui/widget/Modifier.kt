package com.qust.helper.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun Modifier.click(click: () -> Unit): Modifier