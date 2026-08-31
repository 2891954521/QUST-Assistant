package com.qust.helper.next.network.entity.login

import kotlinx.serialization.Serializable

@Serializable
data class EasPublicKey(
	val modulus: String,
	val exponent: String
)