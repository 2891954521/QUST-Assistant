package com.qust.helper.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qust.helper.R
import com.qust.helper.data.Electricity
import com.qust.helper.ui.widget.AppWidgets
import com.qust.helper.ui.widget.Dialogs
import com.qust.helper.ui.widget.Texts
import com.qust.helper.ui.widget.Toast
import com.qust.helper.viewmodel.ElectricRechargeViewModel
import com.qust.helper.viewmodel.ElectricUIEvent
import com.qust.helper.viewmodel.ElectricUIState
import kotlinx.coroutines.launch

object ElectricRecharge {

	val TITLE = arrayOf("电控", "校区", "楼栋", "楼层", "宿舍")

	@Composable
	fun ElectricRecharge(padding: PaddingValues, viewModel: ElectricRechargeViewModel, toast: Toast, navController: NavController) {
		LaunchedEffect(viewModel.uiState.needLogin){
			if(viewModel.uiState.needLogin){
				navController.navigate("vpnLogin")
				viewModel.uiState.needLogin = false
			}
		}
		ElectricRechargeUI(padding = padding, uiState = viewModel.uiState, uiEvent = viewModel.uiEvent)
		AppWidgets.DialogBar(dialogText = viewModel.uiState.dialogText.value)
		toast.ToastContent(viewModel.uiState.toastContent)
	}

	@Composable
	fun ElectricRechargeUI(padding: PaddingValues, uiState: ElectricUIState, uiEvent: ElectricUIEvent) {
		val showAddRoom = remember { mutableStateOf(false) }
		var showRecharge by remember { mutableIntStateOf(-1) }
		var askForDelete by remember { mutableIntStateOf(-1) }

		var selectRoom by remember { mutableStateOf(false) }
		var selectContent by remember { mutableStateOf(Array(5) { "请选择" }) }
		var selectRoomIndex by remember { mutableIntStateOf(0) }

		Column(modifier = Modifier.padding(padding).fillMaxSize()) {
			Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
				Text(text = "卡账户: ${uiState.account}\n卡余额: ${uiState.balance}", modifier = Modifier.align(Alignment.CenterStart))
				TextButton(
					onClick = { uiEvent.refreshCard() },
					modifier = Modifier.align(Alignment.CenterEnd)
				) {
					Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
					Text(text = "刷新卡信息")
				}
			}
			Spacer(Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
			Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
				Text(text = "宿舍列表", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterStart))
				TextButton(
					onClick = { uiEvent.checkNode(); showAddRoom.value = true; },
					modifier = Modifier.align(Alignment.CenterEnd)
				) {
					Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
					Text(text = "添加宿舍")
				}
			}

			LazyColumn{
				itemsIndexed(uiState.rooms) { index, item ->
					ElectricCard(index,
						item,
						refreshBalance = { uiEvent.refreshBalance(it) },
						recharge = { showRecharge = it },
						delete = { askForDelete = it }
					)
				}
			}
		}

		RoomChooseDialog(
			isShow = showAddRoom,
			contents = selectContent,
			onClickItem = {
				if(uiEvent.checkIndex(it)){
					selectRoomIndex = it
					selectRoom = true
				}
		  },
			onConfirm = { uiEvent.addRoom() }
		)

		if(selectRoom){
			Dialogs.ListDialog(TITLE[selectRoomIndex], uiEvent.getIndexName(selectRoomIndex), { selectRoom = false }) { items, index ->
				val tmp = selectContent.clone()
				tmp[selectRoomIndex] = items[index]
				for(i in selectRoomIndex + 1 .. 4) tmp[i] = "请选择"
				selectContent = tmp
				uiEvent.chooseNode(selectRoomIndex, index)
			}
		}

		if(askForDelete != -1){
			Dialogs.AskDialog("删除", "是否删除宿舍: ${uiState.rooms[askForDelete].roomName}", { askForDelete = -1 }){
				uiEvent.deleteRoom(askForDelete)
				askForDelete = -1
			}
		}

		if(showRecharge != -1){
			RechargeDialog(onDismiss = { showRecharge = -1 }){ amount ->
				uiEvent.recharge(showRecharge, amount); showRecharge = -1
			}
		}
	}

	@Composable
	fun ElectricCard(index: Int,
	                 electric: Electricity,
	                 refreshBalance: (Int) -> Unit,
	                 recharge: (Int) -> Unit,
	                 delete: (Int) -> Unit = { }
	) {
		Card(
			modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {
			Box(modifier = Modifier.padding(8.dp)) {
				Column {
					Texts.SingleLineText(
						text = electric.roomName,
						style = MaterialTheme.typography.titleMedium,
						textAlign = TextAlign.Start
					)
					Row(
						verticalAlignment = Alignment.CenterVertically
					) {
						Texts.SingleLineText(
							text = "剩余电量：${electric.balance}",
							textAlign = TextAlign.Start,
							modifier = Modifier.weight(1F)
						)
						TextButton(onClick = { refreshBalance(index) }) {
							Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
							Text(text = "刷新")
						}
						TextButton(onClick = { recharge(index) }) {
							Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
							Text(text = "充值")
						}
					}
				}
				Box(
					modifier = Modifier.align(Alignment.TopEnd).clip(RoundedCornerShape(4.dp)).clickable { delete(index) },
				) {
					Icon(
						imageVector = Icons.Rounded.Close,
						contentDescription = null,
						modifier = Modifier.padding(4.dp)
					)
				}

			}
		}

	}

	@Composable
	@OptIn(ExperimentalMaterial3Api::class)
	fun RoomChooseDialog(
		isShow: MutableState<Boolean>,
		contents: Array<String>,
		onClickItem: (Int) -> Unit = { },
		onConfirm: () -> Unit = { }
	) {
		val scope = rememberCoroutineScope()
		val sheetState = rememberModalBottomSheetState()
		val onDismiss = {
			scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) isShow.value = false; }
		}
		if(isShow.value){
			ModalBottomSheet(
				onDismissRequest = { isShow.value = false; },
				sheetState = sheetState,
			) {
				Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
					Texts.SingleLineTextNoPadding(
						text = "添加宿舍",
						style = MaterialTheme.typography.titleLarge,
						modifier = Modifier.fillMaxWidth()
					)
					for(i in TITLE.indices){
						Box(modifier = Modifier.fillMaxWidth().padding(4.dp).clip(RoundedCornerShape(8.dp)).clickable { onClickItem(i) }) {
							Texts.SingleLineText(text = TITLE[i])
							Texts.SingleLineText(text = contents[i], modifier = Modifier.align(Alignment.CenterEnd))
						}
					}
					Row(modifier = Modifier.fillMaxWidth()) {
						TextButton(onClick = { onDismiss() }, modifier = Modifier.weight(1F)) {
							Texts.SingleLineText(text = stringResource(id = R.string.text_cancel), color = MaterialTheme.colorScheme.error)
						}
						TextButton(onClick = { onDismiss(); onConfirm() }, modifier = Modifier.weight(1F)) {
							Texts.SingleLineText(text = stringResource(id = R.string.text_ok))
						}
					}
					Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
				}
			}
		}
	}

	@Composable
	fun RechargeDialog(
		onDismiss: () -> Unit = { },
		onConfirm: (Int) -> Unit = { }
	){
		var amount by remember { mutableStateOf("") }
		var errorText by remember { mutableStateOf("") }
		AlertDialog(
			onDismissRequest = { onDismiss() },
			title = { Text(text = "请输入充值金额") },
			text = {
				OutlinedTextField(
					value = amount,
					onValueChange = { amount = it },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					label = { Text(text = "金额 (元)") },
					maxLines = 1,
					isError = errorText.isNotEmpty(),
					supportingText = { Text(text = errorText) },
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
				)
			},
			confirmButton = {
				TextButton(onClick = {
					val tmp = ((amount.toFloatOrNull() ?: 0F) * 100).toInt()
					if(tmp == 0){
						errorText = "请输入正确的充值金额"
					}else{
						errorText = ""
						onConfirm(tmp)
					}
				}) {
					Text("充值")
				}
			},
			dismissButton = {
				TextButton(onClick = { onDismiss() }) {
					Text(stringResource(id = R.string.text_cancel))
				}
			}
		)
	}
}