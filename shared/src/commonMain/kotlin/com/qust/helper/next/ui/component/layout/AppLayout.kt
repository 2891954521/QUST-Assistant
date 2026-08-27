package com.qust.helper.next.ui.component.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.theme.color.ContainerColors

@Composable
fun AppContentWithBack(
    title: String,
    contentPadding: PaddingValues? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().then(if(contentPadding != null) Modifier.padding(contentPadding) else Modifier)) {
        SurfaceRow(modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp), colors = ContainerColors.Primary, verticalAlignment = Alignment.CenterVertically) {
            if(onBack != null){
                Box(modifier = Modifier.padding(start = 8.dp).clip(RoundedCornerShape(32.dp)).clickable(onClick = onBack)){
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            Text(
                text = title,
                modifier = Modifier.padding(16.dp),
                style = Theme.textStyles.title,
                maxLines = 1,
            )
        }

        Column(Modifier.weight(1F), content = content)
    }
}

@Composable
fun AppContent(
    title: String,
    contentPadding: PaddingValues? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().then(if(contentPadding != null) Modifier.padding(contentPadding) else Modifier)) {
        SurfaceRow(modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp), colors = ContainerColors.Primary, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
        }
        Column(Modifier.weight(1F), content = content)
    }
}