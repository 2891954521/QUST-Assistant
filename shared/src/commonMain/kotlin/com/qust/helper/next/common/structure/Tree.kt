package com.qust.helper.next.common.structure

data class GroupNode<T>(
    val id: String,
    val children: List<T>
)

data class ChildNode<T>(
    val id: String,
    val value: T
)