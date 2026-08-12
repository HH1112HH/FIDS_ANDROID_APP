package com.example.fidsapp

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val username: String? = null,
    val name: String? = null,
    val clazz: String? = null,
    val phone: String? = null,
    val role: String? = "student",
    val matkhau: String? = null,
    val diaries: List<Diary> = emptyList(),
    val surveys: List<UserSurvey> = emptyList()
)

@Serializable
data class Diary(
    val id: String? = null,
    val username: String? = null,
    val Day: Int? = null,
    val content: String? = null, // Mô tả nhiệm vụ
    val descript: String? = null, // Nội dung người dùng viết
    val Category: String? = null,
    val created_at: String? = null
)

@Serializable
data class UserSurvey(
    val id: String? = null,
    val username: String? = null,
    val content: String? = null,
    val result: String? = null,
    val score: Int? = null,
    val created_at: String? = null
)
