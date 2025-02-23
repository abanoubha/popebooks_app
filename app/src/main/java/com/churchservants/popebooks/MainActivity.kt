package com.churchservants.popebooks

import android.content.Context
import android.content.SharedPreferences
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import java.text.Normalizer
import kotlin.math.max

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
    val sharedPreferences =
        remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }

    NavHost(navController = navController, startDestination = "bookList") {

        composable("bookList") {
            BookListScreen(
                navController = navController,
                db = db,
                sharedPreferences = sharedPreferences,
            )
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
                navController = navController,
                sharedPreferences = sharedPreferences,
            )
        }

        composable(
            route = "searchBookScreen/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getInt("bookId") ?: -1
            SearchBookScreen(
                bookId = bookId,
                db = db,
                navController = navController,
                sharedPreferences = sharedPreferences,
            )
        }

        composable(route = "searchScreen") {
            SearchScreen(
                db = db,
                navController = navController,
                sharedPreferences = sharedPreferences,
            )
        }
    }
}

fun normalizeArabic(text: String): String {
    // Remove diacritics (ḥarakāt)
    val noDiacritics = text.replace(Regex("[\\u064B-\\u0652]"), "") // Remove common harakat range

    // Normalize Unicode (important for consistent comparisons)
    return Normalizer.normalize(noDiacritics, Normalizer.Form.NFC) // or NFD, NFKC, NFKD as needed
}

fun createSummaryWithHighlight(text: String, word: String, limit: Int = 30): String {

    // this doesnt work in Arabic
    //val words = text.split("\\s+").filter { it.isNotBlank() }

    // using Regex works in Arabic
    val pattern = "([\\p{Block=Arabic}]+(?:\\u0020\\u06DB)?)".toRegex()
    val words = pattern.findAll(text).map { it.value }.toList()

    val lowerCaseWord = word.lowercase()
    val normalizedWord = normalizeArabic(lowerCaseWord)

    val wordIndex = words.indexOfFirst {
        val normalizedTextWord = normalizeArabic(it.lowercase())
        normalizedTextWord == normalizedWord
    }

    if (wordIndex == -1) {
        // Word not found, return regular summary
        return if (words.size <= limit) {
            text
        } else {
            words.subList(0, limit).joinToString(" ") + "..." // Truncate and add ellipsis
        }
    }

    val start = max(0, wordIndex - limit / 2) // Start around the word
    val end = kotlin.math.min(words.size, start + limit) // End within bounds

    val summaryWords = words.subList(start, end)
    val summary = summaryWords.joinToString(" ")

    return if (summaryWords.size < words.size) {
        "...$summary..." // Add ellipsis if truncated
    } else {
        summary
    }
}

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

    LaunchedEffect(searchQuery) {
        isLoading = true
        searchResults = searchBookContent(db, bookId, searchQuery)
        sharedPreferences.edit().putString("last_search_query", searchQuery).apply()
        isLoading = false
    }

    BackHandler {
        navController.popBackStack()
    }

    PopebooksTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Search in Book of $bookId") },
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
                    modifier = Modifier.background(color = Color(0xFFD1B000)), // Brownish-Yellowish
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
                } else {

                    TextField(
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
                        placeholder = { Text("Search") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        singleLine = true, // Ensures the text field stays on one line
                    )

                    Text(
                        text = "Search Results (" + searchResults.size + ") :",
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
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text(
                                    "book: " + result.bookName,
                                    fontSize = 18.sp,
                                )
                                Text(
                                    "page: " + result.pageNumber,
                                    fontSize = 16.sp,
                                )
                                Text(
                                    result.pageContent,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }

                }

            }
        }
    }
}

data class BookPage(
    val bookId: Int,
    val bookName: String,
    val pageNumber: Int,
    val pageContent: String
)

fun searchBookContent(db: SQLiteDatabase, bookId: Int, searchQuery: String): List<BookPage> {
    if (searchQuery.isBlank()) {
        return emptyList()
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
    return bookPages
}

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
                    modifier = Modifier.background(color = Color(0xFFD1B000)), // Brownish-Yellowish
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
                    modifier = Modifier.background(color = Color(0xFFD1B000)), // Brownish-Yellowish
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

    LaunchedEffect(searchTerm) {
        isLoading = true
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
                    title = { Text("Search in all books") },
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
                    modifier = Modifier.background(color = Color(0xFFD1B000)), // Brownish-Yellowish
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
                } else {

                    TextField(
                        value = searchTerm,
                        onValueChange = { searchTerm = it },
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
                            if (searchTerm.isNotEmpty()) {
                                IconButton(onClick = { searchTerm = "" }) {
                                    Icon(
                                        painter = rememberVectorPainter(image = Icons.Default.Clear),
                                        contentDescription = "Clear Icon"
                                    )
                                }
                            }
                        },
                        placeholder = { Text("Search") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        singleLine = true, // Ensures the text field stays on one line
                    )

                    Text(
                        text = "Search Results (" + searchResults.size + ") :",
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
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text(
                                    "book: " + result.bookName,
                                    fontSize = 18.sp,
                                )
                                Text(
                                    "page: " + result.pageNumber,
                                    fontSize = 16.sp,
                                )
                                Text(
                                    result.pageContent,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }

                }

            }
        }
    }
}

fun searchAllBooksContent(db: SQLiteDatabase, searchTerm: String): List<BookPage> {
    if (searchTerm.isBlank()) {
        return emptyList()
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
    return bookPages
}
