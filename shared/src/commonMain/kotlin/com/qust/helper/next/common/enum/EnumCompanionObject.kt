package com.qust.helper.next.common.enum

/**
 * 枚举伴生对象接口
 */
interface EnumCompanionObject<T> {

    /**
     * 根据索引获取枚举
     */
    fun getEnumByIndex(index: Int): T

    /**
     * 根据ID获取枚举
     */
    fun getEnumById(id: Int): T

    /**
     * 根据ID获取索引
     */
    fun getIndexById(id: Int): Int

    /**
     * 根据索引获取ID
     */
    fun getIdByIndex(index: Int): Int

    /**
     * 获取所有枚举名称
     */
    fun getNames(): List<String>

}