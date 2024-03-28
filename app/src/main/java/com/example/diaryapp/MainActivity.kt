package com.example.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.diaryapp.data.repository.MongoDB
import com.example.diaryapp.navigation.Screen
import com.example.diaryapp.navigation.SetupNavGraph
import com.example.diaryapp.ui.theme.DiaryAppTheme
import com.example.diaryapp.utils.Constants.APP_ID
import io.realm.kotlin.mongodb.App

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    private var keepSplashOpened = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
//            .setKeepOnScreenCondition {
//            keepSplashOpened
//        }
        WindowCompat.setDecorFitsSystemWindows(window,false)
        setContent {
            DiaryAppTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController,
                    onDataLoaded = {
                      //  keepSplashOpened = false
                    }
                )
            }
        }
    }
}

private fun getStartDestination(): String {
    val user = App.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screen.Home.route
    else Screen.Authentication.route
}
