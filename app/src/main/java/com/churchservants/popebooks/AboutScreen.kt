package com.churchservants.popebooks

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(db: SQLiteDatabase, navController: NavController) {

    val context = LocalContext.current
    var bookCount by remember { mutableIntStateOf(0) }
    var showFeedbackMenu by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackTypeLabel by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        bookCount = getBookCount(db)
    }

    val feedbackOptions = listOf(
        stringResource(R.string.report_bug),
        stringResource(R.string.suggest_feature),
        stringResource(R.string.ask_question),
        stringResource(R.string.give_feedback)
    )

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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
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
            floatingActionButton = {
                Box(contentAlignment = Alignment.TopEnd) {
                    FloatingActionButton(
                        onClick = { showFeedbackMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = stringResource(R.string.feedback_fab)
                        )
                    }

                    DropdownMenu(
                        expanded = showFeedbackMenu,
                        onDismissRequest = { showFeedbackMenu = false }
                    ) {
                        feedbackOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    feedbackTypeLabel = option
                                    showFeedbackMenu = false
                                    showFeedbackDialog = true
                                }
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                )
            }

            if (showFeedbackDialog) {
                AlertDialog(
                    onDismissRequest = { showFeedbackDialog = false },
                    title = { Text(stringResource(R.string.feedback_dialog_title, feedbackTypeLabel)) },
                    text = {
                        OutlinedTextField(
                            value = feedbackMessage,
                            onValueChange = { feedbackMessage = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.feedback_hint)) },
                            minLines = 3
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                sendFeedbackEmail(context, feedbackTypeLabel, feedbackMessage)
                                showFeedbackDialog = false
                                feedbackMessage = ""
                            }
                        ) {
                            Text(stringResource(R.string.send))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showFeedbackDialog = false
                            feedbackMessage = ""
                        }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

fun sendFeedbackEmail(
    context: Context,
    feedbackType: String,
    message: String
) {
    val appVersion = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: Exception) {
        "Unknown"
    }
    val androidVersion = Build.VERSION.RELEASE
    val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
    val locale = Locale.getDefault().toString()

    val emailBody = """
        $message
        
        ---
        App Version: $appVersion
        Android Version: $androidVersion
        Device Model: $deviceModel
        Locale: $locale
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("popebooksapp@abanoubhanna.com"))
        putExtra(Intent.EXTRA_SUBJECT, "[Feedback] PopeBooks Android app - $feedbackType")
        putExtra(Intent.EXTRA_TEXT, emailBody)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, "Send Email"))
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
