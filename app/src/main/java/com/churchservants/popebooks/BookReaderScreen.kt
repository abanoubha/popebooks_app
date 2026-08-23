package com.churchservants.popebooks

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.churchservants.popebooks.ui.theme.PopebooksTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReaderPanel(
    bookId: Int,
    db: SQLiteDatabase,
    modifier: Modifier = Modifier,
    initialPage: Int = 1,
    currentPage: Int = 1,
    scrollState: LazyListState = rememberLazyListState()
) {
    var maxPages by remember(bookId) { mutableIntStateOf(0) }
    LaunchedEffect(bookId) {
        maxPages = withContext(Dispatchers.IO) {
            getMaxPageCount(db, bookId)
        }
    }

    LaunchedEffect(bookId, currentPage, maxPages) {
        if (currentPage > 0 && currentPage <= maxPages) {
            scrollState.scrollToItem(currentPage - 1)
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = modifier.fillMaxSize()
    ) {
        items(maxPages) { index ->
            val pageNumber = index + 1
            val pageContent by produceState<String?>(initialValue = null, bookId, pageNumber) {
                value = withContext(Dispatchers.IO) {
                    loadPageContent(db, bookId, pageNumber)
                }
            }

            Column {
                if (pageContent != null) {
                    Text(
                        text = pageContent!!,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Right,
                        style = TextStyle(textDirection = TextDirection.Content),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
                HorizontalDivider()
            }
        }
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

    val context = LocalContext.current
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }

    LaunchedEffect(Unit) {
        RewardedAd.load(
            context,
            "ca-app-pub-4971969455307153/2790390808",
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
            }
        )
    }

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
                    title = { AppTitle(bookName) },
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
                )
            },
            floatingActionButton = {
                rewardedAd?.let { ad ->
                    FloatingActionButton(
                        onClick = {
                            ad.show(context as android.app.Activity) {
                                // User earned reward
                                rewardedAd = null
                            }
                        },
                        modifier = Modifier.padding(bottom = 60.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Watch Reward Ad"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            ReaderPanel(
                bookId = bookId,
                db = db,
                currentPage = pageNumber,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
