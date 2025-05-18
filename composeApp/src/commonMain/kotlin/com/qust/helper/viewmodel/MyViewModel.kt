package com.qust.helper.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MyViewModel: BaseViewModel() {

	var hasLogin by mutableStateOf(false)

	var userName by mutableStateOf("未登录")
}