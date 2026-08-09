package com.qust.helper.next.ui.component.overlay.dialog

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.qust.helper.next.ui.component.BackHandler
import com.qust.helper.next.ui.component.overlay.OverlayController

@Composable
fun DialogHost(
    controller: OverlayController,
    modifier: Modifier = Modifier
) {
    val currentDialog = controller.dialogs.lastOrNull()

    BackHandler(currentDialog != null){
        if (currentDialog?.dismissOnMaskClick == true) {
            controller.dismissDialog()
        }
    }

    AnimatedVisibility(
        visible = currentDialog != null,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(250)),
        exit = fadeOut(animationSpec = tween(250))
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6F))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    if (currentDialog?.dismissOnMaskClick == true) {
                        controller.dismissDialog()
                    }
                }
            )

            AnimatedVisibility(
                visible = currentDialog != null,
                modifier = Modifier.align(Alignment.Center),
                enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.85f, animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.85f, animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))
            ) {
                Box(modifier = Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    // 拦截内容点击，避免穿透到遮罩
                }) {
                    currentDialog?.content?.invoke()
                }
            }
        }
    }
}
