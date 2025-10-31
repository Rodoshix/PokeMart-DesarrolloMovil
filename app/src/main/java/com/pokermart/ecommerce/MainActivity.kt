package com.pokermart.ecommerce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pokermart.ecommerce.ui.theme.PokeMartTheme
import com.pokermart.ecommerce.di.DI
import com.pokermart.ecommerce.navigation.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DI.inicializar(application)
        setContent {
            PokeMartTheme {
                AppNav()
            }
        }
    }
}
