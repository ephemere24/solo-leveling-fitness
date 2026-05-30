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
