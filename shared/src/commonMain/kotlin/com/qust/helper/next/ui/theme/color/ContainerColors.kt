package com.qust.helper.next.ui.theme.color

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.qust.helper.next.ui.theme.Theme

/**
 * 一个容器的颜色
 *
 * @param background 容器背景色
 * @param content 内容颜色
 */
@Immutable
class ContainerColors(
	val background: Color,
	val content: Color,
) {
	companion object {

		/**
		 * 白底黑字
		 */
		val BlackOnWhite = ContainerColors(
			background = Color(0xFFF5F5F5),
			content = Color(0xFF080808)
		)

		val Primary: ContainerColors
			@Composable
			@ReadOnlyComposable
			get() = ContainerColors(
				background = Theme.color.primary,
				content = Theme.color.onPrimary
			)

		val PrimaryContainer: ContainerColors
			@Composable
			@ReadOnlyComposable
			get() = ContainerColors(
				background = Theme.color.primaryContainer,
				content = Theme.color.onPrimaryContainer
			)

		val Secondary: ContainerColors
			@Composable
			@ReadOnlyComposable
			get() = ContainerColors(
				background = Theme.color.secondary,
				content = Theme.color.onSecondary
			)

		val SecondaryContainer: ContainerColors
			@Composable
			@ReadOnlyComposable
			get() = ContainerColors(
				background = Theme.color.secondaryContainer,
				content = Theme.color.onSecondaryContainer
			)

		val Surface: ContainerColors
			@Composable
			@ReadOnlyComposable
			get() = ContainerColors(
				background = Theme.color.surface,
				content = Theme.color.onSurface
			)

		val Danger: ContainerColors
			@Composable
			@ReadOnlyComposable
			get() = ContainerColors(
				background = Theme.color.error,
				content = Theme.color.onError
			)
	}
}