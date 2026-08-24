package com.qust.helper.next.module.database

import androidx.room3.Room
import com.qust.helper.next.BaseApplication

actual fun createAppDataBase(): AppDataBase {
	return Room
		.databaseBuilder<AppDataBase>(context = BaseApplication.INSTANCE, name = "app_data")
		.build()
}