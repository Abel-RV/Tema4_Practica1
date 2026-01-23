package com.arv.practica1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arv.practica1.ui.NewsScreen
import com.arv.practica1.ui.NewsViewModel
import com.arv.practica1.ui.theme.Tema4_Practica1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiKey = BuildConfig.NEWS_API_KEY
        setContent {
            Tema4_Practica1Theme {
                val viewModel: NewsViewModel= viewModel()
                NewsScreen(viewModel=viewModel,apiKey=apiKey)
            }
        }
    }
}