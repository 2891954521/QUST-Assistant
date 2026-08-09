package com.qust.helper.next.ui.component.overlay.loading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.component.overlay.OverlayController

@Composable
fun LoadingHost(
    controller: OverlayController,
    modifier: Modifier = Modifier
) {

    AnimatedVisibility(
        visible = controller.loadingVisible,
        modifier = modifier,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35F)).clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Loading 默认拦截点击，不关闭
            },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.defaultMinSize(100.dp, 100.dp).padding(32.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x6A000000))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFFCDCDCD))
                    Text(text = controller.loadingText, color = Color(0xFFCDCDCD), modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}