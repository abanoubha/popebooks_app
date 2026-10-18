package com.churchservants.popebooks

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyQuoteWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val quote = QuoteRepository.getQuoteForToday()
        NotificationHelper.showDailyQuoteNotification(applicationContext, quote)
        return Result.success()
    }
}
