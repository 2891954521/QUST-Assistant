package com.qust.helper.next.ui.component.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.qust.helper.next.common.log.Logger


@Composable
fun rememberPermissionState(permissions: List<String>, autoRequest: Boolean = true): PermissionState {
    val activity = LocalActivity.current

    val permissionState = remember(permissions, autoRequest) { PermissionState(permissions, autoRequest) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if(activity != null) permissionState.checkPermission(activity)
    }

    // 自动检查缺失权限
    LaunchedEffect(permissionState.missingPermission) {
        permissionState.launcher = permissionLauncher
        if(activity != null && permissionState.missingPermission == null) permissionState.checkPermission(activity)
    }

    return permissionState
}


class PermissionState(
    val permissions: List<String>,
    val autoRequest: Boolean = true
) {

    var status by mutableStateOf(PermissionStatus.UNKNOWN)

    /**
     * 缺少的权限，为null表示未检查
     */
    var missingPermission by mutableStateOf<List<String>?>(null)

    var launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>? = null

    fun checkPermission(activity: Activity){
        Logger.d("[Permission] check permission: ${permissions.joinToString(", ")}")
        
        val missing = permissions.filter { ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED }

        Logger.d("[Permission] missing: ${missing.joinToString(", ")}")

        missingPermission = missing

        if(missing.isEmpty()){
            status = PermissionStatus.GRANTED
        }else if(autoRequest){
            requestPermission(activity, missing)
        }else {
            status = PermissionStatus.DENIED
        }
    }

    fun requestPermission(activity: Activity, permissions: List<String>? = missingPermission){
        if(permissions.isNullOrEmpty()) return

        if(permissions.any { !ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }){
            status = PermissionStatus.REJECTED
        }else{
            status = PermissionStatus.DENIED
            // 直接申请权限
            launcher?.launch(permissions.toTypedArray())
        }
    }
}

/**
 * 权限授予状态
 */
enum class PermissionStatus {
    /**
     * 未检查（初始值，表示不知道权限是否授予）
     */
    UNKNOWN,

    /**
     * 已授权
     */
    GRANTED,

    /**
     * 未授权（通常指从未被授予，或上次被拒绝但未勾选“不再询问”）
     */
    DENIED,

    /**
     * 被拒绝（用户拒绝且勾选了“不再询问”）
     */
    REJECTED
}