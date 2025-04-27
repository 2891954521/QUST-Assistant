package com.qust.helper.model.database

actual fun getExamStorage(): ExamStorage = ExamStorageImpl()

class ExamStorageImpl: ExamStorage {

}