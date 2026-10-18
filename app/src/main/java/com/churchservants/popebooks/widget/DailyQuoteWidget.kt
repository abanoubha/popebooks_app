package com.churchservants.popebooks.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.churchservants.popebooks.MainActivity
import com.churchservants.popebooks.QuoteRepository
import com.churchservants.popebooks.R

class DailyQuoteWidget : GlanceAppWidget() {

    companion object {
        val BOOK_ID_KEY = ActionParameters.Key<Int>("EXTRA_BOOK_ID")
        val PAGE_NUMBER_KEY = ActionParameters.Key<Int>("EXTRA_PAGE_NUMBER")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val quote = QuoteRepository.getQuoteForToday()

        provideContent {
            GlanceTheme {
                val action = actionStartActivity<MainActivity>(
                    actionParametersOf(
                        BOOK_ID_KEY to quote.bookId,
                        PAGE_NUMBER_KEY to quote.pageNumber
                    )
                )

                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .padding(12.dp)
                        .clickable(action),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Text(
                        text = context.getString(R.string.quote_of_the_day),
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "“${quote.text}”",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 13.sp
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Text(
                        text = "— ${quote.bookName}",
                        style = TextStyle(
                            color = GlanceTheme.colors.secondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
