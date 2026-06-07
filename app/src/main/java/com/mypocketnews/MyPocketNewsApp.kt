package com.mypocketnews

import android.app.Application
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager
import com.mypocketnews.data.db.AppDatabase
import com.mypocketnews.data.settings.SettingsRepository
import com.mypocketnews.notifications.createChannels
import com.mypocketnews.worker.AppWorkerFactory
import okhttp3.OkHttpClient

class MyPocketNewsApp : Application() {
    val okHttpClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }
    val database: AppDatabase by lazy { Room.databaseBuilder(this, AppDatabase::class.java, "mpn.db").build() }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        createChannels(this)
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(AppWorkerFactory(database, settingsRepository, okHttpClient))
                .build()
        )
    }
}
