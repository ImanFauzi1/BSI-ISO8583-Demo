package com.app.edcpoc

import android.app.Application
import android.content.Context
import com.zcs.sdk.card.CardInfoEntity
import dagger.hilt.android.HiltAndroidApp
import android.app.Activity
import android.os.Bundle

@HiltAndroidApp
class MyApp : Application(), Application.ActivityLifecycleCallbacks {

    companion object{
        lateinit var cardInfoEntity: CardInfoEntity
        private lateinit var context: Context
        var currentActivity: Activity? = null

        fun getContext(): Context{
            return context
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        cardInfoEntity = CardInfoEntity()
        context = applicationContext
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        // Don't clear currentActivity here if you want to show dialogs
        // when the app is partially obscured.
    }

    override fun onActivityStopped(activity: Activity) {
        // If the activity that is stopping is the current one, clear it.
        if (currentActivity == activity) {
            //currentActivity = null
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}