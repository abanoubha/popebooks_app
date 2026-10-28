package com.churchservants.popebooks

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import com.churchservants.popebooks.ui.theme.PopebooksTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBookScreen(
    bookId: Int,
    db: SQLiteDatabase,
    navController: NavController,
    sharedPreferences: SharedPreferences
) {
    var searchQuery by remember {
        mutableStateOf(
            sharedPreferences.getString(
                "last_search_query",
                ""
            ) ?: ""
        )
    }

    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf(emptyList<BookPage>()) }
    val bookName by remember { mutableStateOf(getBookName(db = db, bookId = bookId)) }
    var isSearchBarActive by remember { mutableStateOf(false) }
    var performSearch by remember { mutableStateOf(false) }

    var searchedPages by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var searchDurationSec by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        if (searchQuery.isNotBlank() && !hasSearched && !isLoading) {
            performSearch = true
        }
    }

    LaunchedEffect(performSearch) {
        if (!performSearch) return@LaunchedEffect

        val sanitized = sanitize(searchQuery).trim()
        if (sanitized.isEmpty()) {
            searchResults = emptyList()
            hasSearched = false
            isLoading = false
            performSearch = false
            return@LaunchedEffect
        }

        isLoading = true
        hasSearched = false
        searchResults = emptyList()
        searchedPages = 0

        val startTime = System.currentTimeMillis()
        totalPages = withContext(Dispatchers.IO) { getMaxPageCount(db, bookId) }

        val chunkSize = 50
        val mutableResults = mutableListOf<BookPage>()

        if (totalPages > 0) {
            var pageStart = 1
            while (pageStart <= totalPages) {
                val pageEnd = minOf(pageStart + chunkSize - 1, totalPages)
                val chunkResults =
                    searchBookContentPageRange(db, bookId, sanitized, pageStart, pageEnd)
                if (chunkResults.isNotEmpty()) {
                    mutableResults.addAll(chunkResults)
                    searchResults = mutableResults.toList()
                }
                searchedPages = pageEnd
                pageStart += chunkSize
            }
        } else {
            searchResults = searchBookContent(db, bookId, sanitized)
        }

        val elapsedTimeMs = System.currentTimeMillis() - startTime
        searchDurationSec = maxOf(elapsedTimeMs / 1000.0, 0.01)

        sharedPreferences.edit { putString("last_search_query", searchQuery) }
        isLoading = false
        hasSearched = true
        performSearch = false
    }

    BackHandler {
        navController.popBackStack()
    }

    PopebooksTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { AppTitle(stringResource(R.string.search_in, bookName)) },
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
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = innerPadding.calculateTopPadding()),
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        sharedPreferences.edit { putString("last_search_query", searchQuery) }
                    },
                    onSearch = {
                        isSearchBarActive = false
                        searchQuery = it
                        sharedPreferences.edit { putString("last_search_query", searchQuery) }
                        hasSearched = false
                        performSearch = true
                    },
                    active = isSearchBarActive,
                    onActiveChange = {
                        isSearchBarActive = it
                    },
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    trailingIcon = {
                        if (isSearchBarActive) {
                            Icon(
                                modifier = Modifier.clickable {
                                    if (searchQuery.isNotEmpty()) {
                                        searchQuery = ""
                                    } else {
                                        isSearchBarActive = false
                                    }
                                },
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Icon"
                            )
                        }
                    },
                ) { }

                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (totalPages > 0) {
                                stringResource(
                                    R.string.search_progress_pages,
                                    searchedPages,
                                    totalPages
                                )
                            } else {
                                stringResource(R.string.search)
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LinearProgressIndicator(
                            progress = {
                                if (totalPages > 0) searchedPages.toFloat() / totalPages.toFloat() else 0f
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (hasSearched) {
                    val effortText = if (searchResults.isNotEmpty()) {
                        stringResource(
                            R.string.search_effort_single_book,
                            searchResults.size,
                            totalPages,
                            searchDurationSec
                        )
                    } else {
                        stringResource(
                            R.string.search_effort_single_book_none,
                            totalPages,
                            searchDurationSec
                        )
                    }
                    Text(
                        text = effortText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding
                ) {
                    items(searchResults) { result ->
                        Card(
                            onClick = {
                                navController.navigate("bookReader/${result.bookId}/${result.pageNumber}")
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text(
                                stringResource(
                                    R.string.book_page_no,
                                    result.bookName,
                                    result.pageNumber
                                ),
                                fontSize = 16.sp,
                                style = TextStyle(textDirection = TextDirection.Content),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                            val highlightedContent = rememberHighlightAnnotatedString(
                                text = result.pageContent,
                                query = searchQuery
                            )
                            Text(
                                text = highlightedContent,
                                fontSize = 14.sp,
                                style = TextStyle(textDirection = TextDirection.Content),
                                modifier = Modifier.padding(
                                    start = 8.dp,
                                    end = 8.dp,
                                    bottom = 4.dp
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

suspend fun searchBookContentPageRange(
    db: SQLiteDatabase,
    bookId: Int,
    searchQuery: String,
    startPage: Int,
    endPage: Int
): List<BookPage> = withContext(Dispatchers.IO) {
    if (searchQuery.isBlank()) return@withContext emptyList()

    val bookPages = mutableListOf<BookPage>()
    val cursor = db.rawQuery(
        "SELECT b.id, b.name, p.number, p.content FROM books b JOIN pages p ON b.id = p.book_id WHERE b.id = ? AND p.number BETWEEN ? AND ? AND p.content LIKE ?",
        arrayOf(bookId.toString(), startPage.toString(), endPage.toString(), "%$searchQuery%")
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
