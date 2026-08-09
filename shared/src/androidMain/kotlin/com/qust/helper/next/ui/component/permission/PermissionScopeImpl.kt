package com.qust.helper.next.ui.component.permission

import androidx.compose.runtime.Composable

class PermissionScopeImpl: PermissionScope {

    var content: @Composable (() -> Unit)? = null

    var undetermined: @Composable (() -> Unit)? = null

    var notGranted: @Composable (() -> Unit)? = null

    var rejected: @Composable (() -> Unit)? = null

    override fun content(content: @Composable (() -> Unit)){
        this.content = content
    }

    override fun undetermined(content: @Composable (() -> Unit)){
        undetermined = content
    }

    override fun notGranted(content: @Composable (() -> Unit)) {
        notGranted = content
    }

    override fun rejected(content: @Composable (() -> Unit)) {
        rejected = content
    }
}