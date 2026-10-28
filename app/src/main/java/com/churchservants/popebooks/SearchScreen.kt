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
import androidx.navigation.NavController
import com.churchservants.popebooks.ui.theme.PopebooksTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    db: SQLiteDatabase,
    navController: NavController,
    sharedPreferences: SharedPreferences
) {
    var searchTerm by remember {
        mutableStateOf(
            sharedPreferences.getString(
                "last_search_term",
                ""
            ) ?: ""
        )
    }

    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf(emptyList<BookPage>()) }
    var isSearchBarActive by remember { mutableStateOf(false) }
    var performSearch by remember { mutableStateOf(false) }

    var currentBookIndex by remember { mutableIntStateOf(0) }
    var totalBooks by remember { mutableIntStateOf(0) }
    var currentBookName by remember { mutableStateOf("") }
    var matchingBooksCount by remember { mutableIntStateOf(0) }
    var searchDurationSec by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        if (searchTerm.isNotBlank() && !hasSearched && !isLoading) {
            performSearch = true
        }
    }

    LaunchedEffect(performSearch) {
        if (!performSearch) return@LaunchedEffect

        val sanitized = sanitize(searchTerm).trim()
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
        currentBookIndex = 0
        currentBookName = ""
        matchingBooksCount = 0

        val startTime = System.currentTimeMillis()
        val allBooks = withContext(Dispatchers.IO) { loadBooks(db) }
        totalBooks = allBooks.size

        val mutableResults = mutableListOf<BookPage>()

        allBooks.forEachIndexed { index, book ->
            currentBookIndex = index + 1
            currentBookName = book.name
            val bookResults = searchBookContent(db, book.id, sanitized)
            if (bookResults.isNotEmpty()) {
                mutableResults.addAll(bookResults)
                searchResults = mutableResults.toList()
            }
        }

        val elapsedTimeMs = System.currentTimeMillis() - startTime
        searchDurationSec = maxOf(elapsedTimeMs / 1000.0, 0.01)
        matchingBooksCount = mutableResults.map { it.bookId }.distinct().size

        sharedPreferences.edit().putString("last_search_term", searchTerm).apply()
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
                    title = { AppTitle(stringResource(R.string.search_in_all_books)) },
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
                    query = searchTerm,
                    onQueryChange = {
                        searchTerm = it
                        sharedPreferences.edit().putString("last_search_term", searchTerm).apply()
                    },
                    onSearch = {
                        isSearchBarActive = false
                        searchTerm = it
                        sharedPreferences.edit().putString("last_search_term", searchTerm).apply()
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
                                    if (searchTerm.isNotEmpty()) {
                                        searchTerm = ""
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
                            text = if (totalBooks > 0) {
                                stringResource(
                                    R.string.search_progress_books,
                                    currentBookName,
                                    currentBookIndex,
                                    totalBooks
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
                                if (totalBooks > 0) currentBookIndex.toFloat() / totalBooks.toFloat() else 0f
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (hasSearched) {
                    val effortText = if (searchResults.isNotEmpty()) {
                        stringResource(
                            R.string.search_effort_all_books,
                            searchResults.size,
                            matchingBooksCount,
                            totalBooks,
                            searchDurationSec
                        )
                    } else {
                        stringResource(
                            R.string.search_effort_all_books_none,
                            totalBooks,
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
                                query = searchTerm
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

suspend fun searchAllBooksContent(
    db: SQLiteDatabase,
    searchTerm: String
): List<BookPage> = withContext(Dispatchers.IO) {
    if (searchTerm.isBlank()) {
        return@withContext emptyList()
    }

    val bookPages = mutableListOf<BookPage>()

    val cursor = db.rawQuery(
        "SELECT b.id, b.name, p.number, p.content FROM books b JOIN pages p ON b.id = p.book_id WHERE p.content LIKE ?",
        arrayOf("%$searchTerm%")
    )

    cursor.use {
        while (it.moveToNext()) {
            val rBookId = it.getInt(it.getColumnIndexOrThrow("id"))
            val rBookName = it.getString(it.getColumnIndexOrThrow("name"))
            val rPageNumber = it.getInt(it.getColumnIndexOrThrow("number"))

            val rPageContentLong = it.getString(it.getColumnIndexOrThrow("content"))

            val rPageContent = createSummaryWithHighlight(rPageContentLong, searchTerm)

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
