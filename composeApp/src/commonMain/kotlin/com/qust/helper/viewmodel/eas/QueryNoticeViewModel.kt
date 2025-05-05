package com.qust.helper.viewmodel.eas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.entity.eas.Notice
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.eas.NoticeModel

class QueryNoticeViewModel: BaseEasViewModel() {

	var notices by mutableStateOf(emptyList<Notice>())

	var hasRefresh by mutableStateOf(false)
	var refreshing by mutableStateOf(false)

	fun queryNotice() {
		hasRefresh = true
		refreshing = true
		runBackGround {
			try {
				notices = NoticeModel.queryNotice(EasAccount, 1, 20)
			}finally {
				refreshing = false
			}
		}
	}
}