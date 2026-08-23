package com.churchservants.popebooks

import android.content.Context
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewHeadline
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalConfiguration
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
    val configuration = LocalConfiguration.current
    val isTabletLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            configuration.screenWidthDp >= 600

    var currentBookId by rememberSaveable { mutableIntStateOf(initialBookId) }
    var currentBookName by remember { mutableStateOf("") }
    val pagerState = rememberPagerState(initialPage = initialPagerPage, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val readerScrollState = rememberLazyListState()

    val snackbarHostState = remember { SnackbarHostState() }
    val sharedPreferences =
        remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    val hintText = stringResource(R.string.three_panel_hint)

    var fontScale by rememberSaveable {
        mutableFloatStateOf(sharedPreferences.getFloat("font_scale", 1.0f))
    }

    var isBookListVisible by rememberSaveable { mutableStateOf(isTabletLandscape) }
    val showTwoPanes = isTabletLandscape && isBookListVisible

    var currentPageInReader by rememberSaveable { mutableIntStateOf(initialPage) }

    LaunchedEffect(showTwoPanes) {
        if (showTwoPanes && pagerState.currentPage == 0) {
            pagerState.scrollToPage(1)
        }
    }

    LaunchedEffect(currentBookId, currentPageInReader) {
        sharedPreferences.edit {
            putInt("stopped_at_book", currentBookId)
            putInt("stopped_at_page", currentPageInReader)
        }
    }

    LaunchedEffect(fontScale) {
        sharedPreferences.edit {
            putFloat("font_scale", fontScale)
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

    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    PopebooksTheme(fontScale = fontScale) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (pagerState.currentPage == 0 && !showTwoPanes) {
                            AppTitle(stringResource(R.string.titleIn))
                        } else {
                            AppTitle(currentBookName)
                        }
                    },
                    navigationIcon = {
                        if (pagerState.currentPage > 0 && !showTwoPanes) {
                            IconButton(onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
                            }
                        } else if (isTabletLandscape) {
                            IconButton(onClick = {
                                isBookListVisible = !isBookListVisible
                            }) {
                                Icon(
                                    imageVector = if (isBookListVisible) Icons.AutoMirrored.Filled.ViewList else Icons.Default.ViewHeadline,
                                    contentDescription = "Toggle Book List",
                                )
                            }
                        }
                    },
                    actions = {
                        if (pagerState.currentPage == 0 && !showTwoPanes) {
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
                        if (pagerState.currentPage == 1 || showTwoPanes) {
                            if (showTwoPanes) {
                                IconButton(onClick = {
                                    navController.navigate("searchScreen")
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = stringResource(R.string.search_in_all_books),
                                    )
                                }
                            }
                            IconButton(onClick = {
                                fontScale = (fontScale + 0.1f).coerceAtMost(4.0f)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Increase font size",
                                )
                            }
                            IconButton(onClick = {
                                fontScale = (fontScale - 0.1f).coerceAtLeast(1.0f)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = "Decrease font size",
                                )
                            }
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

                Row(modifier = Modifier.weight(1f)) {
                    if (showTwoPanes) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.3f)
                                .padding(start = 24.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            BookListPanel(db = db) { bookId ->
                                currentBookId = bookId
                                currentPageInReader = 1
                                scope.launch {
                                    if (pagerState.currentPage != 1) {
                                        pagerState.animateScrollToPage(1)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(if (showTwoPanes) 0.7f else 1f),
                        userScrollEnabled = true,
                        contentPadding = if (showTwoPanes) PaddingValues(horizontal = 8.dp) else PaddingValues(horizontal = 24.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        if (showTwoPanes && page == 0) {
                            // In two-pane mode, the list is on the left, so we show a placeholder here
                            // Or we could show the search screen? 
                            // But usually, we just want to avoid double list.
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select a book from the list",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
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
                                            db = db,
                                            currentPage = currentPageInReader
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
                    val pageRange = if (showTwoPanes) 1..2 else 0..2
                    repeat(3) { iteration ->
                        if (iteration in pageRange || !showTwoPanes) {
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
}