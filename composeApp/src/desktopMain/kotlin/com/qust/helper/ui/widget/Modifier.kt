package com.qust.helper.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun Modifier.click(click: () -> Unit) = this.clickable(onClick = click)