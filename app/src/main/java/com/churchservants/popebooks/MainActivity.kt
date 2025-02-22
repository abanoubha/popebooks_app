package com.churchservants.popebooks

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.churchservants.popebooks.ui.theme.PopebooksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = remember {
        // Open the database from assets
        context.assets.open("books.db").use { input ->
            val outputFile =
                context.getDatabasePath("books.db") // Get the path where the database will be copied
            outputFile.parentFile?.mkdirs() // Create parent directories if needed
            input.copyTo(outputFile.outputStream()) // Copy the database file
        }
        SQLiteDatabase.openDatabase(
            context.getDatabasePath("books.db").absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
    }

    NavHost(navController = navController, startDestination = "bookList") {

        composable("bookList") {
            BookListScreen(navController, db)
        }

        composable(
            route = "bookReader/{bookId}/{pageNumber}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.IntType },
                navArgument("pageNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getInt("bookId") ?: -1
            val pageNumber = backStackEntry.arguments?.getInt("pageNumber") ?: 1
            BookReaderScreen(
                bookId = bookId,
                pageNumber = pageNumber,
                db = db,
                navController = navController
            )
        }
    }
}

@Composable
fun BookListScreen(navController: NavController, db: SQLiteDatabase) {
    val books = remember { loadBooks(db) }

    PopebooksTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {

                Text(
                    text = stringResource(R.string.titleIn),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )

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

data class Book(val id: Int, val name: String, val pages: Int)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReaderScreen(
    bookId: Int,
    pageNumber: Int,
    db: SQLiteDatabase,
    navController: NavController
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
        isLoading = false
    }

    BackHandler {
        navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
    }

    PopebooksTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                TopAppBar(
                    title = { Text(bookName) },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack(
                                navController.graph.startDestinationId,
                                inclusive = false
                            )
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    modifier = Modifier.background(color = Color(0xFFD1B000)), // Brownish-Yellowish
                )
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

fun getBookName(db: SQLiteDatabase, bookId: Int): String {
    val cursor = db.query("books", null, "id = ?", arrayOf(bookId.toString()), null, null, null)
    cursor.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndexOrThrow("name"))
        }
    }
    return ""
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
