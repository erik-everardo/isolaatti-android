package com.isolaatti

import android.app.Activity
import android.app.Application
import android.os.Bundle

class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

    var startedActivitiesCount = 0

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        startedActivitiesCount++
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        startedActivitiesCount--
    }
}