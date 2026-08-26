package com.qust.helper.next.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.qust.helper.next.ui.viewmodel.EmptyViewModel
import com.qust.helper.next.App
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.theme.Theme

object EmptyPage: BasePage<EmptyViewModel>(title = "", viewModelClass = EmptyViewModel::class) {

    @Composable
    override fun Content(viewModel: EmptyViewModel) {
        val router = App.router

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "空白页", style = Theme.textStyles.titleLarge)

            Button(onClick = {
                router.finish()
            }){
                Text(text = "返回", style = Theme.textStyles.body)
            }
        }
    }

}