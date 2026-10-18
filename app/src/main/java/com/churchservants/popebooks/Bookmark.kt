package com.churchservants.popebooks

data class Bookmark(
    val bookId: Int,
    val bookName: String,
    val pageNumber: Int,
    val snippet: String = ""
)
