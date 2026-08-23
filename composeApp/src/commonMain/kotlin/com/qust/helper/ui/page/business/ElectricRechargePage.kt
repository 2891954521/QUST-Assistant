package com.qust.helper.ui.page.business

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.data.i18n.Strings
import com.qust.helper.entity.business.Electricity
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.Electricity
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.account.IpassLoginPage
import com.qust.helper.ui.page.rememberPageController
import com.qust.helper.ui.widget.ListDialog
import com.qust.helper.ui.widget.AskDialog
import com.qust.helper.viewmodel.business.ElectricRechargeViewModel

object ElectricRechargePage: BasePage<ElectricRechargeViewModel>("电费充值", Drawables.Electricity) {

	val TITLE = arrayOf("电控", "校区", "楼栋", "楼层", "宿舍")

	@Composable
	override fun getViewModel() = viewModel<ElectricRechargeViewModel>()

	@Composable
	override fun Content(viewModel: ElectricRechargeViewModel) {
		val pageController = rememberPageController()
		var showAddRoom by remember { mutableStateOf(false) }
		var showRecharge by remember { mutableIntStateOf(-1) }
		var askForDelete by remember { mutableIntStateOf(-1) }

		var selectRoom by remember { mutableStateOf(false) }
		var selectContent by remember { mutableStateOf(Array(5) { "请选择" }) }
		var selectRoomIndex by remember { mutableIntStateOf(0) }

		LaunchedEffect(viewModel.needLogin) {
			if(viewModel.needLogin) {
				viewModel.needLogin = false
				pageController.startPage(IpassLoginPage)
			}
		}

		Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
			Box(modifier = Modifier.fillMaxWidth()) {
				Text(text = "卡账户: ${viewModel.account}\n卡余额: ${viewModel.balance}", modifier = Modifier.align(Alignment.CenterStart))
				TextButton(
					onClick = { viewModel.refreshCard() },
					modifier = Modifier.align(Alignment.CenterEnd)
				) {
					Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
					Text(text = "刷新卡信息")
				}
			}
			Spacer(Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
			Box(modifier = Modifier.fillMaxWidth()) {
				Text(text = "宿舍列表", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterStart))
				TextButton(
					onClick = { viewModel.checkNode(); showAddRoom = true },
					modifier = Modifier.align(Alignment.CenterEnd)
				) {
					Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
					Text(text = "添加宿舍")
				}
			}

			LazyColumn {
				itemsIndexed(viewModel.rooms) { index, item ->
					ElectricCard(index, item, viewModel, { showRecharge = it }, { askForDelete = it })
				}
			}
		}

		if(showAddRoom) {
			ModalBottomSheetCompat(
				title = "添加宿舍",
				titleList = TITLE,
				contents = selectContent,
				onClickItem = {
					if(viewModel.checkIndex(it)) {
						selectRoomIndex = it
						selectRoom = true
					}
				},
				onConfirm = { viewModel.addRoom() },
				onDismiss = { showAddRoom = false }
			)
		}

		if(selectRoom) {
			ListDialog(
				title = TITLE[selectRoomIndex],
				items = viewModel.getIndexName(selectRoomIndex),
				onDismiss = { selectRoom = false }
			) { items, index ->
				val tmp = selectContent.clone()
				tmp[selectRoomIndex] = items[index]
				for(i in selectRoomIndex + 1 .. 4) tmp[i] = "请选择"
				selectContent = tmp
				viewModel.chooseNode(selectRoomIndex, index)
			}
		}

		if(askForDelete != -1) {
			AskDialog("删除", "是否删除宿舍: ${viewModel.rooms[askForDelete].roomName}", { askForDelete = -1 }) {
				viewModel.deleteRoom(askForDelete)
				askForDelete = -1
			}
		}

		if(showRecharge != -1) {
			RechargeDialog(onDismiss = { showRecharge = -1 }) { amount ->
				viewModel.recharge(showRecharge, amount)
				showRecharge = -1
			}
		}
	}

	@Composable
	fun ElectricCard(
		index: Int,
		electric: Electricity,
		viewModel: ElectricRechargeViewModel,
		recharge: (Int) -> Unit,
		delete: (Int) -> Unit
	) {
		Card(
			modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {
			Box(modifier = Modifier.padding(8.dp)) {
				Column {
					Text(
						text = electric.roomName,
						style = MaterialTheme.typography.titleMedium,
						textAlign = TextAlign.Start
					)
					Row(verticalAlignment = Alignment.CenterVertically) {
						Text(
							text = "剩余电量：${electric.balance}",
							textAlign = TextAlign.Start,
							modifier = Modifier.weight(1F)
						)
						TextButton(onClick = { viewModel.refreshBalance(index) }) {
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
					modifier = Modifier.align(Alignment.TopEnd).clip(RoundedCornerShape(4.dp)).clickable { delete(index) }
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
	fun ModalBottomSheetCompat(
		title: String,
		titleList: Array<String>,
		contents: Array<String>,
		onClickItem: (Int) -> Unit = { },
		onConfirm: () -> Unit = { },
		onDismiss: () -> Unit = { }
	) {
		AlertDialog(
			onDismissRequest = onDismiss,
			title = { Text(text = title) },
			text = {
				Column {
					for(i in titleList.indices) {
						Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp)).clickable { onClickItem(i) }) {
							Text(text = titleList[i], modifier = Modifier.align(Alignment.CenterStart))
							Text(text = contents[i], modifier = Modifier.align(Alignment.CenterEnd))
						}
					}
				}
			},
			confirmButton = {
				TextButton(onClick = { onDismiss(); onConfirm() }) {
					Text(Strings.TEXT_OK)
				}
			},
			dismissButton = {
				TextButton(onClick = onDismiss) {
					Text(Strings.TEXT_CANCEL)
				}
			}
		)
	}

	@Composable
	fun RechargeDialog(
		onDismiss: () -> Unit = { },
		onConfirm: (Int) -> Unit = { }
	) {
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
					if(tmp == 0) {
						errorText = "请输入正确的充值金额"
					} else {
						errorText = ""
						onConfirm(tmp)
					}
				}) {
					Text("充值")
				}
			},
			dismissButton = {
				TextButton(onClick = { onDismiss() }) {
					Text(Strings.TEXT_CANCEL)
				}
			}
		)
	}
}
