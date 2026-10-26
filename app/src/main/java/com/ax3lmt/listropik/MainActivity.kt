package com.ax3lmt.listropik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ax3lmt.listropik.ui.HomeScreen
import com.ax3lmt.listropik.ui.MainViewModel
import com.ax3lmt.listropik.ui.theme.ListropikTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListropikTheme {
                val mainViewModel: MainViewModel = viewModel()
                HomeScreen(viewModel = mainViewModel)
            }
        }
    }
}
