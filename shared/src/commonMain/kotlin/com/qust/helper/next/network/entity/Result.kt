package com.qust.helper.next.network.entity

import kotlinx.serialization.Serializable

@Serializable
data class HttpResult(val code: Int = 500, val msg: String = "")

@Serializable
data class ObjResult<T>(val code: Int = 500, val msg: String = "", val data: T? = null)

@Serializable
data class ListResult<T>(val code: Int = 500, val msg: String = "", val data: List<T> = emptyList())

@Serializable
data class PageResult<T>(val code: Int = 500, val msg: String = "", val data: HttpPage<T> = HttpPage(data = emptyList()))

@Serializable
data class HttpPage<T>(
    val total: Int = 1,
    val data: List<T>
)

@Serializable
data class Id(
    val id: Int = 0
)