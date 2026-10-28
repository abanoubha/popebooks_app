package com.churchservants.popebooks

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.ads.MobileAds
import java.text.Normalizer
import java.util.regex.Pattern

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this) {}
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.scheduleDailyQuoteWork(this)

        val initialBookId = intent.getIntExtra("EXTRA_BOOK_ID", -1)
        val initialPageNumber = intent.getIntExtra("EXTRA_PAGE_NUMBER", -1)

        setContent {
            AppContent(initialBookId, initialPageNumber)
        }
    }
}

@Composable
fun AppContent(initialBookId: Int = -1, initialPageNumber: Int = -1) {
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

    val startDestination = if (initialBookId != -1 && initialPageNumber != -1) {
        "bookReader/$initialBookId/$initialPageNumber"
    } else {
        "adaptiveMain"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable("adaptiveMain") {
            val stoppedAtBook = sharedPreferences.getInt("stopped_at_book", 1)
            val stoppedAtPage = sharedPreferences.getInt("stopped_at_page", 1)
            val stoppedAtPagerPage = sharedPreferences.getInt("stopped_at_pager_page", 0)
            MainAdaptiveScreen(
                db = db,
                navController = navController,
                initialBookId = stoppedAtBook,
                initialPage = stoppedAtPage,
                initialPagerPage = stoppedAtPagerPage
            )
        }

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

        composable(route = "aboutScreen") {
            AboutScreen(
                db = db,
                navController = navController,
            )
        }

        composable(route = "bookmarksScreen") {
            BookmarksScreen(
                navController = navController,
                sharedPreferences = sharedPreferences,
                onSelectBookmark = { bookId, pageNumber ->
                    navController.navigate("bookReader/$bookId/$pageNumber")
                }
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

fun buildArabicFlexibleRegex(query: String): Regex? {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return null
    val sb = java.lang.StringBuilder()
    val harakatPattern = "[\\u064B-\\u0652]*"

    for (i in trimmed.indices) {
        val ch = trimmed[i]
        if (i > 0 && !ch.isWhitespace()) {
            sb.append(harakatPattern)
        }
        when (ch) {
            'ا', 'أ', 'إ', 'آ', 'ٱ' -> sb.append("[اأإآٱ]")
            'ى', 'ي', 'ئ' -> sb.append("[ىيئ]")
            'ه', 'ة' -> sb.append("[هة]")
            ' ' -> sb.append("\\s+")
            else -> sb.append(Regex.escape(ch.toString()))
        }
    }
    return try {
        sb.toString().toRegex(RegexOption.IGNORE_CASE)
    } catch (e: Exception) {
        null
    }
}

fun createSummaryWithHighlight(text: String, word: String, limit: Int = 30): String {
    val trimmedWord = word.trim()
    if (trimmedWord.isEmpty() || text.isEmpty()) {
        return text
    }

    val regex = buildArabicFlexibleRegex(trimmedWord)
    val matchResult = regex?.find(text)

    if (matchResult == null) {
        return if (text.length <= 200) text else text.take(200) + "..."
    }

    val matchStart = matchResult.range.first
    val matchEnd = matchResult.range.last + 1

    val charRadius = limit * 6

    var start = maxOf(0, matchStart - charRadius / 2)
    var end = minOf(text.length, matchEnd + charRadius / 2)

    if (start > 0) {
        val prevSpace = text.lastIndexOf(' ', start)
        if (prevSpace != -1 && prevSpace >= start - 20) {
            start = prevSpace + 1
        }
    }

    if (end < text.length) {
        val nextSpace = text.indexOf(' ', end)
        if (nextSpace != -1 && nextSpace <= end + 20) {
            end = nextSpace
        }
    }

    val snippet = text.substring(start, end).trim()
    val prefix = if (start > 0) "..." else ""
    val suffix = if (end < text.length) "..." else ""

    return "$prefix$snippet$suffix"
}

@Composable
fun rememberHighlightAnnotatedString(
    text: String,
    query: String,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    highlightBackground: Color = MaterialTheme.colorScheme.primaryContainer
): AnnotatedString {
    return remember(text, query, highlightColor, highlightBackground) {
        buildAnnotatedString {
            if (query.isBlank() || text.isBlank()) {
                append(text)
            } else {
                val regex = buildArabicFlexibleRegex(query)
                if (regex == null) {
                    append(text)
                } else {
                    val matches = regex.findAll(text).toList()
                    if (matches.isEmpty()) {
                        append(text)
                    } else {
                        var currentIndex = 0
                        val spanStyle = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = highlightColor,
                            background = highlightBackground
                        )

                        for (match in matches) {
                            val start = match.range.first
                            val end = match.range.last + 1

                            if (start > currentIndex) {
                                append(text.substring(currentIndex, start))
                            }
                            withStyle(spanStyle) {
                                append(text.substring(start, end))
                            }
                            currentIndex = end
                        }

                        if (currentIndex < text.length) {
                            append(text.substring(currentIndex))
                        }
                    }
                }
            }
        }
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

fun sanitize(input: String): String {
    // Regular expression to match allowed characters (English/Arabic text/numbers, whitespace)
    val allowedCharsRegex =
        Pattern.compile("[\\p{L}\\p{N}\\s\\u0600-\\u06FF]+") // Unicode ranges for Arabic

    val sb = StringBuilder()
    var previousWasWhitespace = false

    for (char in input) {
        if (allowedCharsRegex.matcher(char.toString()).matches()) {
            if (char.isWhitespace()) {
                if (!previousWasWhitespace) {
                    sb.append(' ')
                    previousWasWhitespace = true
                }
            } else {
                sb.append(char)
                previousWasWhitespace = false
            }
        }
        // Disallowed characters are simply skipped.
    }

    // Escape special LIKE characters (% and _)
    return sb.toString().replace("%", "\\%").replace("_", "\\_")
}
