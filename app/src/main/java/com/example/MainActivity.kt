package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.smartbus.data.repository.SmartBusRepository
import com.example.smartbus.navigation.SmartBusNavGraph
import com.example.ui.theme.SmartBusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = SmartBusRepository.getInstance(applicationContext)

        setContent {
            SmartBusTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    SmartBusNavGraph(
                        navController = navController,
                        repository = repository
                    )
                }
            }
        }
    }
}
