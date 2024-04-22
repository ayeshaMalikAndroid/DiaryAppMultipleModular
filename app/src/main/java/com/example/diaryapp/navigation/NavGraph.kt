package com.example.diaryapp.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.diaryapp.data.repository.MongoDB
import com.example.util.model.Mood
import com.example.ui.theme.component.DisplayAlertDialog
import com.example.diaryapp.presentation.screens.auth.AuthenticationScreen
import com.example.diaryapp.presentation.screens.auth.AuthenticationViewModel
import com.example.diaryapp.presentation.screens.home.HomeScreen
import com.example.diaryapp.presentation.screens.home.HomeViewModel
import com.example.diaryapp.presentation.screens.write.WriteScreen
import com.example.diaryapp.presentation.screens.write.WriteViewModel
import com.example.diaryapp.utils.Constants.APP_ID
import com.example.diaryapp.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.example.util.model.RequestState
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalMaterial3Api
@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
    onDataLoaded: () -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            },
            onDataLoaded = onDataLoaded
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
            navigateToAuth = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            },
            onDataLoaded = onDataLoaded,
            navigateToWriteArgs = {
                navController.navigate(Screen.Write.passDiaryId(diaryId = it))
            }
        )
        writeRoute(onBackPressed = {
            navController.popBackStack()
            Log.d("writeRoute", "SetupNavGraph: writeRoute is clicked...")
        }
        )
    }

}

//Extension Function
@ExperimentalMaterial3Api
fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()
        val authenticated by viewModel.authenticated
        LaunchedEffect(key1 = Unit) {
            onDataLoaded()
        }
        AuthenticationScreen(
            authenticated = authenticated,
            loadingState = loadingState,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoading(true)
            }, onSuccessfulFirebaseSignIn = { tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated.")
                        viewModel.setLoading(false)
                    },
                    onError = {
                        messageBarState.addError(it)
                        viewModel.setLoading(false)
                    }
                )


            }, onDialogDismissed = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            onFailedFirebaseSignIn = {
                messageBarState.addError(it)
                viewModel.setLoading(false)
            },
            navigateToHome = navigateToHome
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteArgs: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit
) {


    composable(route = Screen.Home.route) {
        val viewModel: HomeViewModel = hiltViewModel()
        //observe diaries from view model
        val diaries by viewModel.diaries
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpened by remember {
            mutableStateOf(false)
        }
        var deleteAllDialogOpened by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(
            diaries = diaries,
            drawerState = drawerState,
            onSignOutClicked = { signOutDialogOpened = true },
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            navigateToWrite = navigateToWrite,
            navigateToWriteArgs = navigateToWriteArgs,
            onDeleteAllClicked = {
                deleteAllDialogOpened = true
            },
            dateIsSelected = viewModel.dateIsSelected,
            onDateSelected = { viewModel.getDiaries(zonedDateTime = it) },
            onDateReset = { viewModel.getDiaries() }

        )
        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to Sign Out from Google Account?",
            dialogOpened = signOutDialogOpened,
            onClosedDialog = { signOutDialogOpened = false },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(Dispatchers.Main) {
                            navigateToAuth
                        }
                    }
                }
            }
        )
        DisplayAlertDialog(
            title = "Delete All Diaries",
            message = "Are you sure you want to permanently delete all your diaries?",
            dialogOpened = deleteAllDialogOpened,
            onClosedDialog = { deleteAllDialogOpened = false },
            onYesClicked = {
                viewModel.deleteAllDiaries(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "All Diaries Deleted.",
                            Toast.LENGTH_SHORT
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onError = {
                        Toast.makeText(
                            context,
                            if (it.message == "No Internet Connectivity.") "We need an Internet Connection for this operation."
                            else it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        )
        LaunchedEffect(key1 = Unit) {
            // able to receive automatically generated schema in our mongoDB Atlas.
            MongoDB.configureTheRealm()
            Log.d("NavGraph", "homeRoute: ${MongoDB.configureTheRealm()}")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(onBackPressed: () -> Unit) {

    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: WriteViewModel = hiltViewModel()
        val uiState = viewModel.uiState
        val context = LocalContext.current
        val galleryState = viewModel.galleryState
        val pagerState = rememberPagerState(pageCount = { Mood.values().size })
        val pagerNumber by remember {
            derivedStateOf { pagerState.currentPage }
        }
        LaunchedEffect(key1 = uiState) {
            Log.d("Selected Diary", "writeRoute: ${uiState.selectedDiaryId}")
        }
        WriteScreen(
            uiState = uiState,
            pagerState = pagerState,
            galleryState = galleryState,
            moodName = { Mood.values()[pagerNumber].name },
            onDeleteConfirmed = {
                viewModel.deleteDiary(onSuccess = {
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }, onError = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                })
            },
            onTitleChanged = { viewModel.setTitle(title = it) },
            onDescriptionChanged = { viewModel.setDescription(description = it) },
            onDateTimeUpdated = { viewModel.updateDateTime(zonedDateTime = it) },
            onBackPressed = onBackPressed,
            onSaveClicked = {
                viewModel.upsertDiary(diary = it.apply { mood = Mood.values()[pagerNumber].name },
                    onSuccess = { onBackPressed() },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onImageSelect = {
                // dynamically get the actual file extension of the Image Uri
                val type = context.contentResolver.getType(it)?.split("/")?.last() ?: "jpg"
                Log.d("WriteViewModel ", "writeRoute:NavGraph Uri: $it")
                viewModel.addImages(image = it, imageType = type)

//                galleryState.addImage(
//                    GalleryImage(image = it, remoteImagePath = "")
//                )
            },
            onImageDeleteClicked = {
                galleryState.removeImage(it)
                Log.d("ImageDeleteClicked", "writeRoute:$it ")
            }
        )
    }

}