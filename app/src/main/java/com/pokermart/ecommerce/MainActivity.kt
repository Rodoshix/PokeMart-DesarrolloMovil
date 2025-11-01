package com.pokermart.ecommerce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pokermart.ecommerce.di.DI
import com.pokermart.ecommerce.navigation.NavGraph
import com.pokermart.ecommerce.ui.theme.PokeMartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DI.inicializar(application)
        setContent {
            PokeMartTheme {
                NavGraph()
            }
        }
    }
}
