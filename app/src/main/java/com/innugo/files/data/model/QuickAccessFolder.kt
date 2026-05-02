package com.innugo.files.data.model

data class QuickAccessFolder(
    val name: String,
    val path: String,
    val itemCount: Int,
    val isPinned: Boolean
)
