package com.sololeveling.fitness

import android.app.Application
import com.google.firebase.FirebaseApp

class SoloLevelingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
