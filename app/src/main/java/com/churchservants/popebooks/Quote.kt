package com.churchservants.popebooks

data class Quote(
    val id: Int,
    val text: String,
    val bookId: Int,
    val bookName: String,
    val pageNumber: Int
)
