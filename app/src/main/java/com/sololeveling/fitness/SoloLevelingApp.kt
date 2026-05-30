package com.sololeveling.fitness

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class SoloLevelingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    companion object {
        val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    }
}
