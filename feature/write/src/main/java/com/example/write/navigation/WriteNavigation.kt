package com.example.write.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.util.Constants
import com.example.util.Screen
import com.example.util.model.Mood
import com.example.write.WriteScreen
import com.example.write.WriteViewModel
import com.google.android.gms.common.util.CollectionUtils

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(onBackPressed: () -> Unit) {

    composable(
        route = Screen.Write.route,
        arguments = CollectionUtils.listOf(navArgument(name = Constants.WRITE_SCREEN_ARGUMENT_KEY) {
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