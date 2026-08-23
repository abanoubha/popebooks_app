package com.churchservants.popebooks

import android.database.sqlite.SQLiteDatabase

fun loadPageContent(db: SQLiteDatabase, bookId: Int, pageNumber: Int): String? {
    val cursor = db.query(
        "pages",
        arrayOf("content"),
        "book_id = ? AND number = ?",
        arrayOf(bookId.toString(), pageNumber.toString()),
        null,
        null,
        null
    )
    cursor.use {
        if (it.moveToNext()) {
            return it.getString(it.getColumnIndexOrThrow("content"))
        }
    }
    return null
}

fun getMaxPageCount(db: SQLiteDatabase, bookId: Int): Int {
    val cursor = db.query(
        "pages",
        arrayOf("COUNT(*)"),
        "book_id = ?",
        arrayOf(bookId.toString()),
        null,
        null,
        null
    )
    cursor.use {
        if (it.moveToFirst()) {
            return it.getInt(it.getColumnIndexOrThrow("COUNT(*)"))
        }
    }
    return 0
}

fun loadBooks(db: SQLiteDatabase): List<Book> {
    val books = mutableListOf<Book>()
    val cursor = db.query("books", null, null, null, null, null, null)
    cursor.use {
        while (it.moveToNext()) {
            val id = it.getInt(it.getColumnIndexOrThrow("id"))
            val name = it.getString(it.getColumnIndexOrThrow("name"))
            books.add(Book(id, name))
        }
    }
    return books
}
