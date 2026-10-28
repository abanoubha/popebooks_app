package com.churchservants.popebooks

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PageListPanel(
    bookId: Int,
    db: SQLiteDatabase,
    currentPage: Int = 1,
    sharedPreferences: SharedPreferences? = null,
    onPageSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val prefs = sharedPreferences ?: remember(context) {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    var maxPages by remember(bookId) { mutableIntStateOf(0) }
    var bookName by remember(bookId) { mutableStateOf("") }
    val scrollState = rememberLazyListState()

    var bookmarkedPages by remember(bookId, prefs) {
        mutableStateOf(loadBookmarks(prefs).filter { it.bookId == bookId }.map { it.pageNumber }.toSet())
    }

    DisposableEffect(prefs, bookId) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "bookmarks_json" && p != null) {
                bookmarkedPages = loadBookmarks(p).filter { it.bookId == bookId }.map { it.pageNumber }.toSet()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    LaunchedEffect(bookId) {
        maxPages = withContext(Dispatchers.IO) {
            getMaxPageCount(db, bookId)
        }
        bookName = withContext(Dispatchers.IO) {
            getBookName(db, bookId)
        }
    }

    LaunchedEffect(currentPage) {
        if (currentPage > 0 && currentPage <= maxPages && !scrollState.isScrollInProgress) {
            scrollState.scrollToItem((currentPage - 5).coerceAtLeast(0))
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(maxPages) { index ->
            val pageNumber = index + 1
            val isCurrentPage = pageNumber == currentPage
            val isBookmarked = bookmarkedPages.contains(pageNumber)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isCurrentPage) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                    .clickable { onPageSelected(pageNumber) }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.book_page_no, "", pageNumber),
                    fontSize = 18.sp,
                    fontWeight = if (isCurrentPage) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isCurrentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        toggleBookmark(prefs, Bookmark(bookId, bookName, pageNumber, ""))
                    }
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) stringResource(R.string.delete_bookmark) else stringResource(R.string.add_bookmark),
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

