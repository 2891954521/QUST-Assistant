package com.qust.helper.next.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.theme.CardStyle
import com.qust.helper.next.ui.theme.color.ContainerColors

@Preview
@Composable
private fun CardPreview() {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		BoxCard(Modifier.padding(8.dp), innerPadding = 4.dp, onClick = { }){
			Text("BoxCard")
		}

		ColumnCard(Modifier.padding(8.dp), innerPadding = 4.dp, onClick = { }){
			Text("ColumnCard")
		}
	}
}

@Preview
@Composable
private fun CardStylesPreview() {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

		BoxCard(style = CardStyle.BackgroundCard, innerPadding = 4.dp) {
			Text("CardContent")
		}

		BoxCard(style = CardStyle.PrimaryContainerCard, innerPadding = 4.dp) {
			Text("CardContent")
		}

		BoxCard(style = CardStyle.SecondaryContainerCard, innerPadding = 4.dp) {
			Text("CardContent")
		}
	}
}

/**
 * 普通卡片
 */
@Composable
fun BoxCard(
	modifier: Modifier = Modifier,
	style: CardStyle = CardStyle.BackgroundCard,
	innerPadding: Dp,
	onClick: (() -> Unit)? = null,
	content: @Composable BoxScope.() -> Unit
){
	BoxCard(
		modifier = modifier,
		style = style,
		innerPadding = PaddingValues(innerPadding),
		onClick = onClick,
		content = content
	)
}

/**
 * 普通卡片
 */
@Composable
fun BoxCard(
	modifier: Modifier = Modifier,
	style: CardStyle = CardStyle.BackgroundCard,
	innerPadding: PaddingValues = PaddingValues.Zero,
	onClick: (() -> Unit)? = null,
	content: @Composable BoxScope.() -> Unit
){
	CompositionLocalProvider(LocalContentColor provides style.contentColor) {
		Box(modifier.card(style).onClick(onClick).padding(innerPadding), content = content)
	}
}

/**
 * 普通卡片
 */
@Composable
fun ColumnCard(
	modifier: Modifier = Modifier,
	style: CardStyle = CardStyle.BackgroundCard,
	innerPadding: Dp,
	onClick: (() -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit
){
	ColumnCard(
		modifier = modifier,
		style = style,
		innerPadding = PaddingValues(innerPadding),
		onClick = onClick,
		content = content
	)
}

/**
 * 普通卡片
 */
@Composable
fun ColumnCard(
	modifier: Modifier = Modifier,
	style: CardStyle = CardStyle.BackgroundCard,
	innerPadding: PaddingValues = PaddingValues.Zero,
	onClick: (() -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit
){
	CompositionLocalProvider(LocalContentColor provides style.contentColor) {
		Column(modifier.card(style).onClick(onClick).padding(innerPadding), content = content)
	}
}


@Composable
fun Modifier.card(colors: CardStyle): Modifier {
	return this
		.then(if(colors.outline != null) Modifier.border(colors.outline, colors.shape) else Modifier)
		.then(when {
			colors.backgroundBrush != null -> Modifier.background(colors.backgroundBrush, colors.shape)
			colors.backgroundColor != Color.Unspecified -> Modifier.background(colors.backgroundColor, colors.shape)
			else -> Modifier
		})
		.clip(colors.shape)
}

