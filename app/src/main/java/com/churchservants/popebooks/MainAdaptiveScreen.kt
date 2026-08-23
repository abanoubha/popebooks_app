package com.churchservants.popebooks

import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.churchservants.popebooks.ui.theme.PopebooksTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.launch

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
    var currentBookId by remember { mutableIntStateOf(initialBookId) }
    var currentBookName by remember { mutableStateOf("") }
    val pagerState = rememberPagerState(initialPage = initialPagerPage, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val readerScrollState = rememberLazyListState()

    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }

    var currentPageInReader by remember { mutableIntStateOf(initialPage) }

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
        currentBookName = getBookName(db, currentBookId)
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
                    userScrollEnabled = true
                ) { page ->
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
                                    readerScrollState.animateScrollToItem(pageNumber - 1)
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}