package com.qust.helper.ui.widget

import androidx.compose.runtime.Composable
import com.qust.helper.viewmodel.extend.LoadingAble

@Composable
fun LoadingUI(loading: LoadingAble){
	if(loading.loadingText.isNotEmpty()) IndeterminateProgressDialog(loading.loadingText)
}