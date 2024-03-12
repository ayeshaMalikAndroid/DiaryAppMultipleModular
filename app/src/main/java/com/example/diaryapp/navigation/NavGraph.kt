package com.example.diaryapp.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.diaryapp.presentation.screens.auth.AuthenticationScreen
import com.example.diaryapp.presentation.screens.auth.AuthenticationViewModel
import com.example.diaryapp.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState

@ExperimentalMaterial3Api
@Composable
fun SetupNavGraph(startDestination: String, navController: NavHostController) {

    NavHost(navController = navController, startDestination = startDestination) {
        authenticationRoute()
        homeRoute()
        writeRoute()
    }

}

//Extension Function
@ExperimentalMaterial3Api
fun NavGraphBuilder.authenticationRoute() {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()
        AuthenticationScreen(
            oneTapState = oneTapState,
            loadingState = oneTapState.opened,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoading(true)
            }, onTokenIdReceived = {tokenId->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                           if (it){
                               messageBarState.addSuccess("Successfully Authenticated.")
                           }
                  viewModel.setLoading(false)
                    },
                    onError = {
                        messageBarState.addError(it)
                    }
                )


            }, onDialogDismissed = {message->
                messageBarState.addError(Exception(message))
            }
        )
    }
}

fun NavGraphBuilder.homeRoute() {
    composable(route = Screen.Home.route) {

    }
}

//fun NavGraphBuilder.writeRoute() {
//    composable(route = Screen.Write.route, arguments =
//    listOf(navArgument(name =WRITE_SCREEN_ARGUMENT_KEY){
//        type = NavType.StringType
//        nullable = true
//        defaultValue = null
//    })
//    ) {
//
//    }

@Suppress("DEPRECATION")
fun NavGraphBuilder.writeRoute() {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {

    }

}