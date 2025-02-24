package com.churchservants.popebooks

data class BookPage(
    val bookId: Int,
    val bookName: String,
    val pageNumber: Int,
    val pageContent: String
)