package com.example.marshal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.marshal.presentation.CheckListNote
import com.example.marshal.presentation.HomeScreen
import com.example.marshal.presentation.navigation.AppNavigation
import com.example.marshal.ui.theme.MarshalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarshalTheme {
                AppNavigation()
            }
        }
    }
}

