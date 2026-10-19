package com.qust.helper.next.ui.component.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.qust.helper.next.common.Platform
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.theme.color.ContainerColors

@Composable
fun AppContentWithBack(
    title: String,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SurfaceBox(modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp), colors = ContainerColors.Primary) {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().then(if(Platform.isJVM()) Modifier.padding(16.dp) else Modifier.padding(start = 16.dp, bottom = 16.dp)), verticalAlignment = Alignment.CenterVertically) {
                if(onBack != null){
                    Box(modifier = Modifier.clip(RoundedCornerShape(32.dp)).clickable(onClick = onBack)){
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Text(text = title, modifier = Modifier.padding(start = 16.dp), style = Theme.textStyles.bodyLarge, maxLines = 1)
            }
        }

        Column(Modifier.weight(1F), content = content)
    }
}

@Composable
fun AppContent(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SurfaceBox(modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp), colors = ContainerColors.Primary) {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().then(if(Platform.isJVM()) Modifier.padding(16.dp) else Modifier.padding(start = 16.dp, bottom = 16.dp)), verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, modifier = Modifier.padding(start = 16.dp), style = Theme.textStyles.bodyLarge, maxLines = 1)
            }
        }
        Column(Modifier.weight(1F), content = content)
    }
}