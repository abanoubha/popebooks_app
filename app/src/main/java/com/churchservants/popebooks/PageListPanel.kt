package com.churchservants.popebooks

import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PageListPanel(
    bookId: Int,
    db: SQLiteDatabase,
    onPageSelected: (Int) -> Unit
) {
    var maxPages by remember(bookId) { mutableIntStateOf(0) }
    LaunchedEffect(bookId) {
        maxPages = withContext(Dispatchers.IO) {
            getMaxPageCount(db, bookId)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(maxPages) { index ->
            val pageNumber = index + 1
            Text(
                text = stringResource(R.string.book_page_no, "", pageNumber),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPageSelected(pageNumber) }
                    .padding(16.dp)
            )
            HorizontalDivider()
        }
    }
}