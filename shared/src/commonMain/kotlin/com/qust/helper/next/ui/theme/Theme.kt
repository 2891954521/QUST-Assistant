package com.qust.helper.next.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.qust.helper.next.ui.component.LocalScaleProvider
import com.qust.helper.next.ui.theme.color.AppColors
import com.qust.helper.next.ui.theme.color.LocalColor


/**
 * 应用主题
 */
object Theme {

    /**
     * 应用颜色
     */
    inline val color: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalColor.current

    /**
     * 应用预设文本样式
     */
    inline val textStyles: AppTypography
        @Composable @ReadOnlyComposable
        get() = LocalTypography.current

    /**
     * 基础文本样式，用于从该基础样式创建不同的样式
     */
    inline val textStyle: TextStyle
        @Composable @ReadOnlyComposable
        get() = LocalTypography.current.style


    /**
     * 容器内容颜色
     */
    inline val contentColor: Color
        @Composable @ReadOnlyComposable
        get() = LocalContentColor.current

}

@Composable
fun AppThemeProvider(content: @Composable () -> Unit) {
    LocalScaleProvider {
        CompositionLocalProvider(
            LocalColor provides AppColors(),

            LocalTypography provides AppTypography(TextStyle(
                fontWeight = FontWeight.Normal,
                color = Color.White
            )),

            LocalContentColor provides Color.White,
        ) {
            MaterialTheme(
                colorScheme = LocalColor.current.colorScheme,
                typography = Theme.textStyles.typography,
            ) {
                PlatformAppTheme(content = content)
            }
        }
    }
}

@Composable
expect inline fun PlatformAppTheme(noinline content: @Composable () -> Unit)