package com.churchservants.popebooks

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.churchservants.popebooks.ui.theme.PopebooksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PopebooksTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                BookListScreen()
//                    FlippingText(
//                        text = "Hello, Word!",
//                        modifier = Modifier.padding(innerPadding),
//                    )
//                }
            }
        }
    }
}

@Composable
fun BookListScreen() {
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
    var selectedBookId by remember { mutableIntStateOf(-1) }
    val books = remember { loadBooks(db) }

    if (selectedBookId == -1) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                text = "كتب البابا شنودة",
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
                        selectedBookId = book.id
                    }
                    HorizontalDivider()
                }
            }
        }
    } else {
        BookReaderScreen(bookId = selectedBookId, db = db, onBack = { selectedBookId = -1 })
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
fun BookReaderScreen(bookId: Int, db: SQLiteDatabase, onBack: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(1) }
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

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(bookName) },
            navigationIcon = {
                IconButton(onClick = onBack) {
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
                            if (dragAmount > 50) { // Swipe Right (Previous Page)
                                if (currentPage > 1) {
                                    currentPage--
                                    isLoading = true
                                }
                            } else if (dragAmount < -50) { // Swipe Left (Next Page)
                                if (currentPage < maxPages) {
                                    currentPage++
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
                    colors = ButtonDefaults.buttonColors(Color(0xFFB58863)),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                    )
//                    Text("Previous")
                }
                Text(
                    "$currentPage / $maxPages",
                    color = Color.Black,
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
                    colors = ButtonDefaults.buttonColors(Color(0xFFB58863)),
                ) {
                    Text("Next")
                }
            }
        } else {
            // Handle cases where page content is not found
            Text("Error loading page. Please try again.", textAlign = TextAlign.Center)
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

@Composable
fun FlippingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(fontSize = 20.sp)
) {
    var rotation by remember { mutableFloatStateOf(0f) }
    var targetRotation by remember { mutableFloatStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 300)
    )

    Box(modifier = modifier) {
        Text(
            text = text,
            style = style,
            modifier = Modifier
                .graphicsLayer {
                    rotationX = animatedRotation
                    cameraDistance = 8f // Adjust for 3D effect depth
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount > 0) { // Swiping right
                            targetRotation -= 180f // Flip 180 degrees
                        } else if (dragAmount < 0) { // Swiping left
                            targetRotation += 180f // Flip 180 degrees
                        }
                    }
                }
        )
    }
}
