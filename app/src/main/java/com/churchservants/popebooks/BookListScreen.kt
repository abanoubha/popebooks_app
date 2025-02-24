package com.churchservants.popebooks

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.churchservants.popebooks.ui.theme.PopebooksTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    navController: NavController,
    db: SQLiteDatabase,
    sharedPreferences: SharedPreferences
) {
    val books = remember { loadBooks(db) }
    val stoppedAtBook by remember {
        mutableIntStateOf(
            sharedPreferences.getInt(
                "stopped_at_book",
                1
            ) ?: 1
        )
    }
    val stoppedAtPage by remember {
        mutableIntStateOf(
            sharedPreferences.getInt(
                "stopped_at_page",
                1
            ) ?: 1
        )
    }

    PopebooksTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.titleIn),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp),
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("bookReader/$stoppedAtBook/$stoppedAtPage")
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_bookmarked),
                                contentDescription = "search in the book's content",
                            )
                        }
                        IconButton(onClick = {
                            navController.navigate("searchScreen")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "search in all books",
                            )
                        }
                    },
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(books) { book ->
                        BookItem(book) {
                            navController.navigate("bookReader/${book.id}/1")
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun BookItem(book: Book, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            book.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f), // to have the full width, so it can be RTL by the content direction
            style = TextStyle(textDirection = TextDirection.Content),
        )
    }
}

fun loadBooks(db: SQLiteDatabase): List<Book> {
    val books = mutableListOf<Book>()
    val cursor = db.query("books", null, null, null, null, null, null)
    cursor.use {
        while (it.moveToNext()) {
            val id = it.getInt(it.getColumnIndexOrThrow("id"))
            val name = it.getString(it.getColumnIndexOrThrow("name"))
            val pages = it.getInt(it.getColumnIndexOrThrow("pages"))
            books.add(Book(id, name, pages))
        }
    }
    return books
}
