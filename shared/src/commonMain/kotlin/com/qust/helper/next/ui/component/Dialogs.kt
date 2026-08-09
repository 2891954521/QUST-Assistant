package com.qust.helper.next.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.component.overlay.LocalOverlayController
import com.qust.helper.next.App
import com.qust.helper.next.ui.theme.color.ContainerColors
import com.qust.helper.next.ui.theme.Theme
import java.util.UUID


/**
 * 基础模态框，不提供任何内容，仅有一个空白的Dialog
 */
@Composable
fun BaseDialog(
    visible: Boolean,
    onDismiss: () -> Unit = { },
    colors: ContainerColors = ContainerColors.BlackOnWhite,
    buttonContent: @Composable RowScope.() -> Unit = { },
    content: @Composable ColumnScope.() -> Unit,
) {
    val overlay = LocalOverlayController.current
    val dialogId = remember { UUID.randomUUID().toString() }

    val onDismiss by rememberUpdatedState(onDismiss)
    val content by rememberUpdatedState(content)

    DisposableEffect(overlay, dialogId) {
        onDispose {
            overlay.dismissDialog(dialogId)
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            overlay.showDialog(
                id = dialogId,
                dismissOnMaskClick = false,
                onDismiss = onDismiss,
                content = {
                    BaseDialog(
                        colors = colors,
                        buttonContent = buttonContent,
                        content = content
                    )
                }
            )
        } else {
            overlay.dismissDialog(dialogId)
        }
    }
}

@Composable
fun BaseDialog(
    colors: ContainerColors = ContainerColors.BlackOnWhite,
    buttonContent: @Composable RowScope.() -> Unit = { },
    content: @Composable ColumnScope.() -> Unit,
) {
    Box {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.background, contentColor = colors.content),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            CompositionLocalProvider(LocalContentColor provides colors.content){
                DialogContent({
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, content = content)
                }, {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, content = buttonContent)
                })
            }
        }
    }
}


/**
 * 确认对话框，包含标题、消息、取消按钮和确认按钮
 *
 * @param visible 对话框是否显示
 * @param title 对话框标题，默认为空，即无标题
 * @param message 对话框内容信息，必须提供
 * @param onDismiss 对话框关闭时触发的回调函数
 * @param onConfirm 确认按钮点击时的回调函数
 * @param cancelText 取消按钮显示的文字
 * @param okText 确认按钮显示的文字
 * @param colors 对话框颜色配置
 * @param buttonContent 按钮区域额外的内容
 */
@Composable
fun ConfirmDialog(
    visible: Boolean,
    title: String? = null,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    cancelText: String = "取消",
    okText: String = "确定",
    colors: ContainerColors = ContainerColors.BlackOnWhite,
    buttonContent: @Composable (RowScope.() -> Unit)? = null,
){
    ConfirmDialog(
        visible = visible,
        title = title,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        cancelText = cancelText,
        okText = okText,
        buttonContent = buttonContent
    ) {
        Text(text = message, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
    }
}


/**
 * 确认对话框，带有取消和确认按钮的模态对话框。
 *
 * @param visible 对话框是否显示
 * @param title 对话框标题，默认为空，即无标题
 * @param onDismiss 对话框关闭时触发的回调函数
 * @param onConfirm 确认按钮点击时的回调函数
 * @param cancelText 取消按钮的文字
 * @param okText 确认按钮的文字
 * @param colors 对话框颜色配置
 * @param buttonContent 按钮区域额外的内容
 * @param content 对话框主体内容的组成部分，需提供一个自定义的布局
 */
@Composable
fun ConfirmDialog(
    visible: Boolean,
    title: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    cancelText: String = "取消",
    okText: String = "确定",
    colors: ContainerColors = ContainerColors.BlackOnWhite,
    buttonContent: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
){
    BaseDialog(
        visible = visible,
        onDismiss = onDismiss,
        colors = colors,
        buttonContent = {
            buttonContent?.invoke(this)
            PrimaryButton(modifier = Modifier.padding(24.dp), text = cancelText, onClick = onDismiss)
            PrimaryButton(modifier = Modifier.padding(24.dp), text = okText, colors = ContainerColors.Secondary, onClick = onConfirm)
        }
    ) {
        if(title != null) Text(text = title, style = Theme.textStyles.titleMedium, maxLines = 1)
        content()
    }
}


/**
 * 消息对话框，只有一个确认按钮的模态对话框
 *
 * @param visible 对话框是否显示
 * @param title 对话框标题，默认为空，即无标题
 * @param message 对话框中显示的消息内容
 * @param onDismiss 对话框关闭时触发的回调函数
 * @param okText 确认按钮上的文本
 * @param colors 对话框颜色配置
 * @param buttonContent 按钮区域额外的内容
 */
@Composable
fun MessageDialog(
    visible: Boolean,
    title: String? = null,
    message: String,
    onDismiss: () -> Unit,
    okText: String = "确定",
    colors: ContainerColors = ContainerColors.BlackOnWhite,
    buttonContent: @Composable (RowScope.() -> Unit)? = null,
){
    MessageDialog(
        visible = visible,
        title = title,
        onDismiss = onDismiss,
        buttonContent = buttonContent,
        okText = okText
    ) {
        Text(text = message, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
    }
}


/**
 * 消息对话框，只有一个确认按钮的模态对话框
 *
 * @param visible 对话框是否显示
 * @param onDismiss 对话框关闭时触发的回调函数
 * @param okText 确认按钮上的文本
 * @param colors 对话框颜色配置
 * @param buttonContent 按钮区域额外的内容
 * @param content 对话框中显示的主要内容部分，需提供一个自定义的布局
 */
@Composable
fun MessageDialog(
    visible: Boolean,
    title: String? = null,
    onDismiss: () -> Unit,
    okText: String = "确定",
    colors: ContainerColors = ContainerColors.BlackOnWhite,
    buttonContent: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
){
    BaseDialog(
        visible = visible,
        onDismiss = onDismiss,
        colors = colors,
        buttonContent = {
            buttonContent?.invoke(this)
            PrimaryButton(modifier = Modifier.padding(24.dp), text = okText, onClick = onDismiss)
        }
    ) {
        if(title != null) Text(text = title, style = Theme.textStyles.titleMedium, maxLines = 1)
        content()
    }
}


@Composable
private fun DialogContent(content: @Composable () -> Unit, options: @Composable () -> Unit) {
    val scale = App.scale

    SubcomposeLayout(measurePolicy = { constraints ->

        val minWidth = scale.screenWidth / 2
        val maxWidth = scale.screenWidth / 10 * 9

        val heightUnit = scale.screenHeight / 10

        val optionsPre = subcompose("options_pre", options).first().measure(constraints.copy(
            minWidth = minWidth,
            maxWidth = maxWidth,
            maxHeight = heightUnit * 2
        ))

        val contentPlaceAble = subcompose("content", content).first().measure(constraints.copy(
            minWidth = minWidth,
            maxWidth = maxWidth,
            maxHeight = heightUnit * 8 - optionsPre.height
        ))

        val optionsPlaceAble = subcompose("options", options).first().measure(constraints.copy(
            minWidth = contentPlaceAble.width,
            maxWidth = contentPlaceAble.width,
            maxHeight = heightUnit * 8 - contentPlaceAble.height
        ))

        layout(contentPlaceAble.width, contentPlaceAble.height + optionsPlaceAble.height) {
            contentPlaceAble.place(0, 0)
            optionsPlaceAble.place(0, contentPlaceAble.height)
        }
    })
}
