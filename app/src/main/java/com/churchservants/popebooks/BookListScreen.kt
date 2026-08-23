package com.churchservants.popebooks

import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.churchservants.popebooks.ui.theme.PopebooksTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

@Composable
fun BookListPanel(
    db: SQLiteDatabase,
    onBookSelected: (Int) -> Unit
) {
    val books = remember { loadBooks(db) }
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(books) { book ->
            BookItem(book) {
                onBookSelected(book.id)
            }
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    navController: NavController,
    db: SQLiteDatabase,
    sharedPreferences: SharedPreferences
) {
    val context = LocalContext.current
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

    PopebooksTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        AppTitle(stringResource(R.string.titleIn))
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
                        IconButton(onClick = {
                            navController.navigate("aboutScreen")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "about app",
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                rewardedAd?.let { ad ->
                    FloatingActionButton(onClick = {
                        ad.show(context as android.app.Activity) {
                            // User earned reward
                            rewardedAd = null // Reset for next time if needed, or just leave it
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Watch Reward Ad"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                BannerAdView(adUnitId = "ca-app-pub-4971969455307153/9009932763")
                BookListPanel(db = db) { bookId ->
                    navController.navigate("bookReader/$bookId/1")
                }
            }
        }
    }
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
