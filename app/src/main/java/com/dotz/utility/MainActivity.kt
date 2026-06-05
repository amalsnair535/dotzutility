package com.dotz.utility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dotz.utility.ui.navigation.DotzNavGraph
import com.dotz.utility.ui.theme.DotzTheme
import com.dotz.utility.ui.theme.LocalThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DotzTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DotzNavGraph()
                }
            }
        }
    }
}
