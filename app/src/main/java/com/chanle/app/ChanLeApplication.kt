package com.chanle.app

import android.app.Application
import android.content.Context
import com.chanle.app.data.RecipeRepository
import com.chanle.app.data.ai.AiClient
import com.chanle.app.data.ai.AiSettings
import com.chanle.app.data.ai.RecipeGenerator
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val repository = RecipeRepository(appContext)
    val aiSettings = AiSettings(appContext)
    val aiClient = AiClient(aiSettings)
    val recipeGenerator = RecipeGenerator(aiClient, aiSettings)
}

class ChanLeApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        AppCenter.start(
            this,
            "64313361-c13d-4ff9-bef6-9b188434d65a",
            Analytics::class.java,
            Crashes::class.java,
            Distribute::class.java
        )
    }
}