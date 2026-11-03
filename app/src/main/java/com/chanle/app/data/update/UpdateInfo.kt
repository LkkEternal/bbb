package com.chanle.app.data.update

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfo(
    val version: String,
    val versionCode: Int,
    val content: String,
    val downloadUrl: String,
    val isForce: Boolean = false,
    val fileSize: String? = null
)

@Serializable
data class UpdateResponse(
    val latest: UpdateInfo,
    val minVersion: UpdateInfo? = null
)