package com.churchservants.popebooks

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.churchservants.popebooks.ui.theme.PopebooksTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReaderScreen(
    bookId: Int,
    pageNumber: Int,
    db: SQLiteDatabase,
    navController: NavController,
    sharedPreferences: SharedPreferences
) {
    var currentPage by remember { mutableIntStateOf(pageNumber) }
    var maxPages by remember { mutableIntStateOf(0) }
    var pageContent by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var bookName by remember { mutableStateOf("") }

    LaunchedEffect(bookId) {
        maxPages = getMaxPageCount(db, bookId)
        bookName = getBookName(db, bookId)
    }

    LaunchedEffect(bookId, currentPage) {
        isLoading = true
        pageContent = loadPageContent(db, bookId, currentPage)
        sharedPreferences.edit().putInt("stopped_at_book", bookId).apply()
        sharedPreferences.edit().putInt("stopped_at_page", currentPage).apply()
        isLoading = false
    }

    BackHandler {
        navController.popBackStack()
    }

    PopebooksTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(bookName) },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("searchBookScreen/$bookId")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "search in the book's content",
                            )
                        }
                    },
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                } else if (pageContent != null) {
                    val scrollState = rememberScrollState()

                    Text(
                        text = pageContent!!,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Right,
                        style = TextStyle(textDirection = TextDirection.Content),
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    if (dragAmount > 50) { // Swipe Right (Next Page)
                                        if (currentPage < maxPages) {
                                            currentPage++
                                            isLoading = true
                                        }
                                    } else if (dragAmount < -50) { // Swipe Left (Previos Page)
                                        if (currentPage > 1) {
                                            currentPage--
                                            isLoading = true
                                        }
                                    }
                                }
                            },
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = {
                                if (currentPage > 1) {
                                    currentPage--
                                }
                            },
                            enabled = currentPage > 1 && !isLoading,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.previous_btn),
                            )
                            Text(stringResource(R.string.previous_btn))
                        }
                        Text(
                            "$currentPage / $maxPages",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = {
                                if (currentPage < maxPages) {
                                    currentPage++
                                }
                            },
                            enabled = currentPage < maxPages && !isLoading,
                        ) {
                            Text(stringResource(R.string.next_btn))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.next_btn),
                            )
                        }
                    }
                } else {
                    // Handle cases where page content is not found
                    Text("Error loading page. Please try again.", textAlign = TextAlign.Center)
                }

            }
        }
    }
}

fun getMaxPageCount(db: SQLiteDatabase, bookId: Int): Int {
// this code is correct, but I did not include pages count in the "pages" column yet
//    val cursor =
//        db.query("books", arrayOf("pages"), "id = ?", arrayOf(bookId.toString()), null, null, null)
//    cursor.use {
//        if (it.moveToFirst()) {
//            return it.getInt(it.getColumnIndexOrThrow("pages"))
//        }
//    }
//    return 0

    // I'll use this code temporarily to get the max page number
    // SELECT COUNT(*) FROM pages WHERE book_id = ?
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

suspend fun searchBookContent(
    db: SQLiteDatabase,
    bookId: Int,
    searchQuery: String
): List<BookPage> =
    withContext(Dispatchers.IO) {
        if (searchQuery.isBlank()) {
            return@withContext emptyList()
        }

        val bookPages = mutableListOf<BookPage>()

        val cursor = db.rawQuery(
            "SELECT b.id, b.name, p.number, p.content FROM books b JOIN pages p ON b.id = p.book_id WHERE b.id = ? AND p.content LIKE ?",
            arrayOf(bookId.toString(), "%$searchQuery%")
        )

        cursor.use {
            while (it.moveToNext()) {
                val rBookId = it.getInt(it.getColumnIndexOrThrow("id"))
                val rBookName = it.getString(it.getColumnIndexOrThrow("name"))
                val rPageNumber = it.getInt(it.getColumnIndexOrThrow("number"))

                val rPageContentLong = it.getString(it.getColumnIndexOrThrow("content"))

                val rPageContent = createSummaryWithHighlight(rPageContentLong, searchQuery)

                bookPages.add(
                    BookPage(
                        rBookId,
                        rBookName,
                        rPageNumber,
                        rPageContent
                    )
                )
            }
        }
        return@withContext bookPages
    }

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
