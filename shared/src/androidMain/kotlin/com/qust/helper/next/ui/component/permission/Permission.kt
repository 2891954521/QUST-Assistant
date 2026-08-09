package com.qust.helper.next.ui.component.permission

import androidx.compose.runtime.*
import com.qust.helper.next.ui.component.permission.PermissionStatus.*


/**
 * 权限检查组件，根据权限授予状态展示不同内容
 *
 * 权限检查组件，有权限直接展示content，无权限就申请，用户拒绝后展示noPermission
 */
@Composable
fun Permission(permissionState: PermissionState, content: PermissionScope.() -> Unit) {
	val customContent by rememberUpdatedState(content)

	val content by remember { derivedStateOf { PermissionScopeImpl().apply(customContent) } }

	when(permissionState.status){
        UNKNOWN -> content.undetermined?.invoke()
        GRANTED -> content.content?.invoke()
        DENIED -> content.notGranted?.invoke()
        REJECTED -> content.rejected?.invoke()
    }
}