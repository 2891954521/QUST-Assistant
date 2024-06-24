package com.qust.helper.ui.page.business

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.qust.helper.R
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.model.account.VpnAccount
import com.qust.helper.viewmodel.BaseViewModel

/*

/api/tzjc/score/detail?studentNumber=2029740103&year=2024
{"avg":"173","score2":"","isCheat":false,"level":"","score1":"","projectName":"身高","percent":""},

/api/basic/school/year/list
{"status":1,"message":"ok","result":[{"text":"2024-2025","value":2024},{"text":"2023-2024","value":2023}]}

GET /api/basic/school/year/task-list?year=2023
{"name":"体质检测2023","testType":0,"id":1}

GET /api/tzjc/score/upload-list?studentNumber=2029740101&year=2023&taskId=1
{"studentId":9693,"score":"6'05\"","uploadId":129114,"studentNumber":"2029740101","projectNumber":6,"testTime":1685766592000,"projectName":"耐力跑","taskId":1},

 */



object SportTest {

	val SportTestPage = Page(Keys.Page.SportTestPage, "智慧体测", R.drawable.ic_school){ activity, padding, navController, _ ->
		val viewModel by activity.viewModels<SportTestViewModel>()
//		SportTestUI(padding, viewModel, activity.toast, navController)
	}

	@Composable
	fun SportTestUI() {
//		LaunchedEffect(viewModel.uiState.needLogin){
//			if(viewModel.uiState.needLogin){
//				navController.navigate(Keys.Page.VpnLoginPage)
//				viewModel.uiState.needLogin = false
//			}
//		}
//		ElectricRecharge.ElectricRechargeUI(padding = padding, uiState = viewModel.uiState, uiEvent = viewModel.uiEvent)
//		AppWidgets.DialogBar(dialogText = viewModel.uiState.dialogText.value)
//		toast.ToastContent(viewModel.uiState.toastContent)
	}

	class SportTestViewModel : BaseViewModel() {
		private val vpnAccount = VpnAccount("tiyu.qust.edu.cn", "https")
	}
}