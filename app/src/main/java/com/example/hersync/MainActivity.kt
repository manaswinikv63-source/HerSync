package com.example.hersync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.hersync.ui.HerSyncApp
import com.example.hersync.ui.theme.Charcoal
import com.example.hersync.ui.theme.HerSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HerSyncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Charcoal,
                ) {
                    HerSyncApp()
                }
            }
        }
    }
}
