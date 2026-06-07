package com.mypocketnews.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.mypocketnews.MyPocketNewsApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MyPocketNewsApp
        val startDestination = intent.getStringExtra("start_destination")

        setContent {
            MaterialTheme {
                Surface {
                    AppNavHost(
                        app = app,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
