package com.qust.helper.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.qust.helper.ui.widget.DialogAble
import com.qust.helper.ui.widget.DialogAbleImpl
import com.qust.helper.ui.widget.ToastAble
import com.qust.helper.ui.widget.ToastAbleImpl

abstract class BaseViewModel: ViewModel(), ToastAble by ToastAbleImpl(), DialogAble by DialogAbleImpl()

abstract class BaseAndroidViewModel(application: Application): AndroidViewModel(application), ToastAble by ToastAbleImpl(), DialogAble by DialogAbleImpl()