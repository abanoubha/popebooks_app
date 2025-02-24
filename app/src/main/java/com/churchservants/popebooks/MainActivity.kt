package com.churchservants.popebooks

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.text.Normalizer
import kotlin.math.max
import kotlin.math.min

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
    val end = min(words.size, start + limit) // End within bounds

    val summaryWords = words.subList(start, end)
    val summary = summaryWords.joinToString(" ")

    return if (summaryWords.size < words.size) {
        "...$summary..." // Add ellipsis if truncated
    } else {
        summary
    }
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
