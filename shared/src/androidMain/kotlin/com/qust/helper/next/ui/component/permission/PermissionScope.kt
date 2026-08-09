package com.qust.helper.next.ui.component.permission

import androidx.compose.runtime.Composable

interface PermissionScope {

    /**
     * 权限正常授予的内容
     */
    fun content(content: @Composable () -> Unit)

    /**
     * 权限未检查时的占位内容
     */
    fun undetermined(content: @Composable (() -> Unit))

    /**
     * 无权限时显示的内容
     */
    fun notGranted(content: @Composable () -> Unit)

    /**
     * 权限被用户显式拒绝时的内容
     */
    fun rejected(content: @Composable () -> Unit)
}