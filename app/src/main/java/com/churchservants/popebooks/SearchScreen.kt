package com.churchservants.popebooks

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    var searchResults by remember { mutableStateOf(emptyList<BookPage>()) }
    var isSearchBarActive by remember { mutableStateOf(false) }
    var performSearch by remember { mutableStateOf(false) }

    if (!performSearch && searchResults.isEmpty() && searchTerm != "") {
        performSearch = true
    }

    LaunchedEffect(performSearch) {
        if (!performSearch) return@LaunchedEffect

        isLoading = true
        searchTerm = sanitize(searchTerm)
        searchResults = searchAllBooksContent(db, searchTerm)
        sharedPreferences.edit().putString("last_search_term", searchTerm).apply()
        isLoading = false
    }

    BackHandler {
        navController.popBackStack()
    }

    PopebooksTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.search_in_all_books)) },
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
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    query = searchTerm,
                    onQueryChange = {
                        searchTerm = it
                        sharedPreferences.edit().putString("last_search_term", searchTerm).apply()
                    },
                    onSearch = {
                        isSearchBarActive = false
                        searchTerm = it
                        sharedPreferences.edit().putString("last_search_term", searchTerm).apply()
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

                Text(
                    text = stringResource(R.string.search_results, searchResults.size),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                    ) {
                        items(searchResults) { result ->
                            Card(
                                onClick = {
                                    navController.navigate("bookReader/${result.bookId}/${result.pageNumber}")
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
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
                                Text(
                                    result.pageContent,
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
