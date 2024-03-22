package com.qust.helper.ui.common

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.data.Data
import com.qust.helper.ui.activity.ComposeActivity
import com.qust.helper.ui.theme.TEXT_COLORS
import kotlinx.coroutines.launch


@Composable
fun MainAppDrawer(drawerState: DrawerState){
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	Column(modifier = Modifier.padding(16.dp)) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = stringResource(R.string.app_name),
				modifier = Modifier.padding(16.dp).weight(1F),
				style = MaterialTheme.typography.titleLarge,
				maxLines = 1,
			)
			Image(
				painter = painterResource(id = R.mipmap.ic_launcher),
				contentDescription = null,
				modifier = Modifier.padding(16.dp)
			)
		}

		Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
			var index = 0
			for(page in Data.Pages.values){
				NavigationDrawerItem(
					label = { Text(text = page.name) },
					selected = false,
					icon = {
						Icon(
							painter = if(page.image != null) rememberVectorPainter(page.image) else painterResource(id = page.iconRes),
							contentDescription = page.name,
							tint = Color(TEXT_COLORS[index++ % (TEXT_COLORS.size - 1) + 1])
						)
					},
					onClick = {
						scope.launch { drawerState.apply { if(isOpen) close() } }
						context.startActivity(Intent(context, ComposeActivity::class.java).putExtra("page", page.key))
					}
				)
			}
		}
	}
}