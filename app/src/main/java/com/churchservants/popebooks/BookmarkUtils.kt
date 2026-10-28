package com.churchservants.popebooks

import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

private const val KEY_BOOKMARKS = "bookmarks_json"

fun loadBookmarks(prefs: SharedPreferences): List<Bookmark> {
    val jsonString = prefs.getString(KEY_BOOKMARKS, null) ?: return emptyList()
    val bookmarks = mutableListOf<Bookmark>()
    try {
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val bookId = obj.getInt("bookId")
            val bookName = obj.optString("bookName", "")
            val pageNumber = obj.getInt("pageNumber")
            val snippet = obj.optString("snippet", "")
            bookmarks.add(Bookmark(bookId, bookName, pageNumber, snippet))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return bookmarks
}

fun saveBookmarks(prefs: SharedPreferences, bookmarks: List<Bookmark>) {
    val array = JSONArray()
    for (b in bookmarks) {
        val obj = JSONObject().apply {
            put("bookId", b.bookId)
            put("bookName", b.bookName)
            put("pageNumber", b.pageNumber)
            put("snippet", b.snippet)
        }
        array.put(obj)
    }
    prefs.edit { putString(KEY_BOOKMARKS, array.toString()) }
}

fun isBookmarked(prefs: SharedPreferences, bookId: Int, pageNumber: Int): Boolean {
    return loadBookmarks(prefs).any { it.bookId == bookId && it.pageNumber == pageNumber }
}

fun toggleBookmark(prefs: SharedPreferences, bookmark: Bookmark): Boolean {
    val bookmarks = loadBookmarks(prefs).toMutableList()
    val existingIndex =
        bookmarks.indexOfFirst { it.bookId == bookmark.bookId && it.pageNumber == bookmark.pageNumber }
    val isAdded: Boolean
    if (existingIndex != -1) {
        bookmarks.removeAt(existingIndex)
        isAdded = false
    } else {
        bookmarks.add(0, bookmark)
        isAdded = true
    }
    saveBookmarks(prefs, bookmarks)
    return isAdded
}

fun removeBookmark(prefs: SharedPreferences, bookId: Int, pageNumber: Int) {
    val bookmarks =
        loadBookmarks(prefs).filterNot { it.bookId == bookId && it.pageNumber == pageNumber }
    saveBookmarks(prefs, bookmarks)
}
