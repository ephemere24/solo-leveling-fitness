package com.sololeveling.fitness

import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.sololeveling.fitness.ui.screens.*
import com.sololeveling.fitness.ui.theme.*
import com.sololeveling.fitness.viewmodel.GameViewModel
import android.content.Intent

class MainActivity : ComponentActivity() {

    companion object {
        var crashInfo: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Global crash handler — never closes, shows error screen
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sb = StringBuilder()
            sb.appendLine("FATAL ERROR in ${thread.name}")
            sb.appendLine(throwable.toString())
            for (el in throwable.stackTrace) sb.appendLine("  at $el")
            val cause = throwable.cause
            if (cause != null) {
                sb.appendLine("Caused by: ${cause}")
                for (el in cause.stackTrace.take(5)) sb.appendLine("  at $el")
            }
            val errorStr = sb.toString()
            crashInfo = errorStr
            android.util.Log.e("SLF_CRASH", errorStr)
            try {
                val i = Intent(this, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
                Process.killProcess(Process.myPid())
            } catch (_: Exception) {
                Process.killProcess(Process.myPid())
            }
        }

        crashInfo = intent.getStringExtra("saved_crash")
        val currentCrash = crashInfo

        setContent {
            SoloLevelingTheme {
                if (currentCrash != null) {
                    ErrorScreen(currentCrash) {
                        crashInfo = null
                        recreate()
                    }
                } else {
                    AppRootScreen()
                }
            }
        }
    }
}
