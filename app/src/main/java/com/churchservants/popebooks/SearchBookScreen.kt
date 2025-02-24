package com.churchservants.popebooks

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.churchservants.popebooks.ui.theme.PopebooksTheme

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
    var searchResults by remember { mutableStateOf(emptyList<BookPage>()) }
    val bookName by remember { mutableStateOf(getBookName(db = db, bookId = bookId)) }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length > 2) {
            isLoading = true
            searchResults = searchBookContent(db, bookId, searchQuery)
            sharedPreferences.edit().putString("last_search_query", searchQuery).apply()
            isLoading = false
        } else {
            note = "search query must be more than 2 letters"
        }
    }

    BackHandler {
        navController.popBackStack()
    }

    PopebooksTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.search_in, bookName)) },
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
                if (isLoading) {
                    TextField(
                        isError = note != "", // show error hint
                        enabled = false,
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        leadingIcon = {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Default.Search),
                                contentDescription = "Search Icon"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        painter = rememberVectorPainter(image = Icons.Default.Clear),
                                        contentDescription = "Clear Icon"
                                    )
                                }
                            }
                        },
                        placeholder = { Text(stringResource(R.string.search)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        singleLine = true, // Ensures the text field stays on one line
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.outlineVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )

                    Text(
                        note,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    Text(
                        text = stringResource(R.string.search_results, searchResults.size),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )

                    CircularProgressIndicator(
                        modifier = Modifier
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                } else {

                    TextField(
                        isError = note != "", // show error hint
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        leadingIcon = {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Default.Search),
                                contentDescription = "Search Icon"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        painter = rememberVectorPainter(image = Icons.Default.Clear),
                                        contentDescription = "Clear Icon"
                                    )
                                }
                            }
                        },
                        placeholder = { Text(stringResource(R.string.search)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        singleLine = true, // Ensures the text field stays on one line
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.outlineVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )

                    Text(
                        note,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    Text(
                        text = stringResource(R.string.search_results, searchResults.size),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )

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
