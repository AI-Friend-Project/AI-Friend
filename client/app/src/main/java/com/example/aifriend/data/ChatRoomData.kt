package com.example.aifriend.data

import android.text.Editable
import java.sql.Time
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 채팅방 데이터 클래스
 */
data class ChatRoomData(
    var name: String? = null,
    var message: String? = null,
    var time: Date? = null,
    var uid: String? = null
)
