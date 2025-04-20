package com.qust.helper.entity.vo

import kotlinx.serialization.Serializable

@Serializable
data class EasPublicKey(
	val modulus: String
)