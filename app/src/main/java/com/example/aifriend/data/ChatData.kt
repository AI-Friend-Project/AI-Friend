package com.example.aifriend.data

/**
 * 채팅 리스트 데이터 클래스
 */

data class ChatData(
    var lastChat: String? = null,
    val uid: ArrayList<String?>? = null,
    var name: ArrayList<String?>? = null,
    var time: String? = null,
    var key: String? = null)
