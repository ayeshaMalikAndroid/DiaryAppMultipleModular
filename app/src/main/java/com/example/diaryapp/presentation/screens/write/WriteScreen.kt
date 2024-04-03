package com.example.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.diaryapp.model.Diary
import com.example.diaryapp.model.GalleryState
import com.example.diaryapp.model.Mood
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    uiState: UiState,
    pagerState: PagerState,
    moodName: () -> String,
    galleryState: GalleryState,
    onDeleteConfirmed: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onBackPressed: () -> Unit,
    onDateTimeUpdated: (ZonedDateTime) -> Unit,
    onSaveClicked: (Diary) -> Unit,
    onImageSelect :(Uri) -> Unit

) {
    //update the mood when selecting an exiting diary from home screen
    LaunchedEffect(key1 = uiState.mood) {
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    }
    Scaffold(
        topBar = {
            WriteTopBar(
                selectedDiary = uiState.selectedDiary,
                moodName = moodName,
                onDeleteConfirmed = onDeleteConfirmed,
                onBackPressed = onBackPressed,
                onDateTimeUpdated = onDateTimeUpdated

            )
        },
        content = {
            WriteContent(
                pagerState = pagerState,
                title = uiState.title,
                uiState = uiState,
                galleryState = galleryState,
                onTitleChanged = onTitleChanged,
                description = uiState.description,
                onDescriptionChanged = onDescriptionChanged,
                paddingValues = it,
                onSaveClicked = onSaveClicked,
                onImageSelect = onImageSelect
            )
        }
    )
}