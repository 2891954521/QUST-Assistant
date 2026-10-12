package com.qust.helper.next.module.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.qust.helper.next.common.appDatabasePath
import com.qust.helper.next.common.log.Logger

actual fun createAppDataBase(): AppDataBase {
	val path = appDatabasePath()
	Logger.i("database: $path")
	return Room
		.databaseBuilder<AppDataBase>(path)
		.setDriver(BundledSQLiteDriver())
		.build()
}
