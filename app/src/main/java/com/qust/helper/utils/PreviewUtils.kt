package com.qust.helper.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.qust.helper.data.eas.Academic
import com.qust.helper.ui.page.eas.GetAcademic

@Preview(showBackground = true)
@Composable
fun NoticeItemPreview() {
	GetAcademic.ItemUI(Academic.LessonInfo(name = "高等数学",
		content = "讲课（3.0）",
		status = 4, mark = "100"), true) {
		
	}
}