package com.qust.helper.next.module.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun createAppDataBase(): AppDataBase {
	return Room
		.databaseBuilder<AppDataBase>("app_data")
		.setDriver(BundledSQLiteDriver())
		.build()
}
