package com.example.aifriend.data

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

/**
 * 채팅 리스트 데이터 클래스
 */

data class ChatData(
    var lastChat: String? = null,
    val uid: ArrayList<String?>? = null,
    var name: ArrayList<String?>? = null,
    var time: Date? = null,
    var key: String? = null,
    var check: ArrayList<Int?>? = null  // 0 : 안읽음 ,  1: 읽음
)
