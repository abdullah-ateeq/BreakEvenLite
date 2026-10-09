package com.example.breakevenlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.breakevenlite.ui.theme.BreakEvenLiteTheme
import com.example.breakevenlite.ui.viewmodel.BreakEvenViewModel
import com.example.breakevenlite.ui.viewmodel.BreakEvenViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: BreakEvenViewModel by viewModels {
        BreakEvenViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()

            BreakEvenLiteTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                BreakEvenApp(viewModel = viewModel)
            }
        }
    }
}