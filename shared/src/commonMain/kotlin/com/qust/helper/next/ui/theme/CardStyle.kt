package com.qust.helper.next.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.theme.color.AppColors
import com.qust.helper.next.ui.theme.color.Colors


/**
 * 卡片容器样式
 *
 * @param backgroundColor 容器背景色
 * @param backgroundBrush 容器背景笔刷，与背景色同时存在时笔刷优先生效
 * @param outline 容器描边
 * @param shape 容器形状
 * @param contentColor 内容颜色
 */
@Immutable
class CardStyle(
    val backgroundColor: Color = Color.Unspecified,
    val backgroundBrush: Brush? = null,
    val outline: BorderStroke? = null,
    val shape: Shape = RectangleShape,
    val contentColor: Color,
) {
    /**
     * 卡片样式模板
     *
     * 样式预览: [CardStylesPreview][com.qust.helper.next.ui.component.CardStylesPreview]
     */
    companion object {

        /**
         * 背景卡片
         */
        val BackgroundCard: CardStyle
            @Composable
            @ReadOnlyComposable
            get() = CardStyle(
                backgroundColor = Theme.color.background,
                shape = RoundedCornerShape(8.dp),
                contentColor = Theme.color.onBackground
            )

        val PrimaryContainerCard: CardStyle
            @Composable
            @ReadOnlyComposable
            get() = CardStyle(
                backgroundColor = Theme.color.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                contentColor = Theme.color.onPrimaryContainer
            )

        val SecondaryContainerCard: CardStyle
            @Composable
            @ReadOnlyComposable
            get() = CardStyle(
                backgroundColor = Theme.color.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                contentColor = Theme.color.secondaryContainer
            )
    }
}