package com.churchservants.popebooks

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import com.churchservants.popebooks.ui.theme.PopebooksTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAdaptiveScreen(
    db: SQLiteDatabase,
    navController: NavController,
    initialBookId: Int = 1,
    initialPage: Int = 1,
    initialPagerPage: Int = 1
) {
    val context = LocalContext.current
    var currentBookId by rememberSaveable { mutableIntStateOf(initialBookId) }
    var currentBookName by remember { mutableStateOf("") }
    val pagerState = rememberPagerState(initialPage = initialPagerPage, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val readerScrollState = rememberLazyListState()

    val snackbarHostState = remember { SnackbarHostState() }
    val sharedPreferences =
        remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    val hintText = stringResource(R.string.three_panel_hint)

    var currentPageInReader by rememberSaveable { mutableIntStateOf(initialPage) }

    LaunchedEffect(currentBookId, currentPageInReader) {
        sharedPreferences.edit {
            putInt("stopped_at_book", currentBookId)
            putInt("stopped_at_page", currentPageInReader)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        sharedPreferences.edit {
            putInt("stopped_at_pager_page", pagerState.currentPage)
        }
    }

    LaunchedEffect(readerScrollState) {
        snapshotFlow { readerScrollState.firstVisibleItemIndex }
            .collect { index ->
                val newPage = index + 1
                if (currentPageInReader != newPage) {
                    currentPageInReader = newPage
                }
            }
    }

    LaunchedEffect(Unit) {
        val hintShown = sharedPreferences.getBoolean("three_panel_hint_shown", false)
        if (!hintShown) {
            snackbarHostState.showSnackbar(hintText)
            sharedPreferences.edit { putBoolean("three_panel_hint_shown", true) }
        }
    }

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

    LaunchedEffect(currentBookId) {
        currentBookName = withContext(Dispatchers.IO) {
            getBookName(db, currentBookId)
        }
    }

    PopebooksTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (pagerState.currentPage == 0) {
                            AppTitle(stringResource(R.string.titleIn))
                        } else {
                            AppTitle(currentBookName)
                        }
                    },
                    actions = {
                        if (pagerState.currentPage == 0) {
                            IconButton(onClick = {
                                navController.navigate("searchScreen")
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = stringResource(R.string.search_in_all_books),
                                )
                            }
                            IconButton(onClick = {
                                navController.navigate("aboutScreen")
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = stringResource(R.string.about_app),
                                )
                            }
                        }
                        if (pagerState.currentPage == 1) {
                            IconButton(onClick = {
                                navController.navigate("searchBookScreen/$currentBookId")
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "search in the book's content",
                                )
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                rewardedAd?.let { ad ->
                    FloatingActionButton(
                        onClick = {
                            ad.show(context as android.app.Activity) {
                                rewardedAd = null
                            }
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Watch Reward Ad"
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                BannerAdView(adUnitId = "ca-app-pub-4971969455307153/9009932763")

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    userScrollEnabled = true,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        when (page) {
                            0 -> {
                                BookListPanel(db = db) { bookId ->
                                    currentBookId = bookId
                                    currentPageInReader = 1
                                    scope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                }
                            }

                            1 -> {
                                ReaderPanel(
                                    bookId = currentBookId,
                                    db = db,
                                    currentPage = currentPageInReader,
                                    scrollState = readerScrollState
                                )
                            }

                            2 -> {
                                PageListPanel(
                                    bookId = currentBookId,
                                    db = db
                                ) { pageNumber ->
                                    scope.launch {
                                        currentPageInReader = pageNumber
                                        // Jump immediately to the page to avoid long animations blocking the UI
                                        readerScrollState.scrollToItem(pageNumber - 1)
                                        // Then animate the pager to show the reader
                                        pagerState.animateScrollToPage(1)
                                    }
                                }
                            }
                        }
                    }
                }

                // Pager Indicator hint
                Row(
                    Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { iteration ->
                        val color = if (pagerState.currentPage == iteration)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
            }
        }
    }
}