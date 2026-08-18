package com.churchservants.popebooks

import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.churchservants.popebooks.ui.theme.PopebooksTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(db: SQLiteDatabase, navController: NavController) {

    var bookCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        bookCount = getBookCount(db)
    }

    val annotatedText = buildAnnotatedString {

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(stringResource(R.string.app_name))
        }
        append("\n")

        append(stringResource(R.string.app_description))
        append("\n\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(stringResource(R.string.developer_name))
        }
        append("\n")

        append(stringResource(R.string.developer_info))
        append("\n\n")

        append(stringResource(R.string.books))
        append(bookCount.toString())
        append(stringResource(R.string.nbooks))
        append("\n\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(stringResource(R.string.contact_abanoub_hanna_via))
        }
        append("\n")

        append("◉ ")
        withLink(
            LinkAnnotation.Url(
                url = "https://www.linkedin.com/in/abanoub-hanna/",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.linkedin))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://x.com/abanoubha",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.x_account))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://youtube.com/@abanoubha",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.youtube))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://github.com/abanoubha",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.github))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://t.me/abanoubchan",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.telegram))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://facebook.com/AbanoubHannaDotCom",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.facebook_page))
        }

        append("\n\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(stringResource(R.string.mobile_apps_i_developed))
        }
        append("\n")

        append("◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://play.google.com/store/apps/details?id=com.softwarepharaoh.popebooks",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.pope_books))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://play.google.com/store/apps/details?id=com.softwarepharaoh.popebooks",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.pope_books))
            append(stringResource(R.string.by_abanoub_ha))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://play.google.com/store/apps/details?id=com.softwarepharaoh.agpeya",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.agpeya_app))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://play.google.com/store/apps/details?id=com.kartbusiness.app",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.kart_business_digital_cards))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://play.google.com/store/apps/details?id=com.abanoubhanna.pdf",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.keme_pdf_all_in_one_tools))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://play.google.com/store/apps/details?id=com.abanoubhanna.kemecash",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.keme_cash_finance_task_book))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://play.google.com/store/apps/details?id=com.abanoubhanna.ocr",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.ocr_text_scanner))
        }

        append("\n◉ ")

        withLink(
            LinkAnnotation.Url(
                url = "https://play.google.com/store/apps/details?id=com.softwarepharaoh.img2txt",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSecondary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(stringResource(R.string.img2txt_pdf_image_to_text))
        }

        append("\n")
    }

    PopebooksTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.about_app),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                )
            }
        }
    }
}

suspend fun getBookCount(db: SQLiteDatabase): Int = withContext(Dispatchers.IO) {
    val tableName = "books"
    val countColumnAlias = "book_count"

    // SELECT COUNT(*) AS book_count FROM books
    val cursor = db.query(
        tableName,
        arrayOf("COUNT(*) AS $countColumnAlias"),
        null,
        null,
        null,
        null,
        null
    )

    cursor.use {
        if (it.moveToFirst()) {
            return@withContext it.getInt(it.getColumnIndexOrThrow(countColumnAlias))
        }
    }
    return@withContext 0
}
