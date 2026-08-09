package com.qust.helper.next.ui.component

import androidx.compose.runtime.Composable


@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)