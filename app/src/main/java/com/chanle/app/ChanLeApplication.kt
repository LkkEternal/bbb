package com.chanle.app

import android.app.Application
import android.content.Context
import com.chanle.app.data.RecipeRepository
import com.chanle.app.data.ai.AiClient
import com.chanle.app.data.ai.AiSettings
import com.chanle.app.data.ai.RecipeGenerator

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
    }
}