package com.example.aifriend.data

import android.text.Editable
import java.time.LocalDateTime

/**
 * 채팅방 데이터 클래스
 */
data class ChatRoomData(
    var name: String? = null,
    var message: String? = null,
    var time: String? = null,
    var uid: String? = null
)
