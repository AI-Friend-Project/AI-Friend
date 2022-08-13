package com.example.aifriend.data

data class ChatData(
    var lastChat: String? = null,
    val uid: ArrayList<String?>? = null,
    var name: String? = null,
    var key: String? = null)
    /*val users: HashMap<String, Boolean> = HashMap(),
    val comments : HashMap<String, Comment> = HashMap()) {
    class Comment(val message: String? = null,
                  val uid: String? = null,
                  val time: String? = null)
}*/