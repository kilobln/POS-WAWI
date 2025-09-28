package com.example.poswawi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.poswawi.ui.LocalCafeRepository
import com.example.poswawi.ui.POSNavHost
import com.example.poswawi.ui.PosWawiAppTheme
import com.example.poswawi.ui.rememberPosWawiAppState

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        val app = application as PosWawiApp
        MainViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PosWawiAppTheme {
                CompositionLocalProvider(LocalCafeRepository provides viewModel.repository) {
                    val state = rememberPosWawiAppState()
                    Surface(color = MaterialTheme.colorScheme.background) {
                        POSNavHost(appState = state)
                    }
                }
            }
        }
    }
}

class MainViewModel(val repository: com.example.poswawi.data.CafeRepository) : ViewModel() {
    companion object {
        fun Factory(repository: com.example.poswawi.data.CafeRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(repository) as T
                }
            }
    }
}
