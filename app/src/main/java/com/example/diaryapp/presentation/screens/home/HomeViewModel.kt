package com.example.diaryapp.presentation.screens.home

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.repository.Diaries
import com.example.diaryapp.data.repository.MongoDB
import com.example.diaryapp.utils.RequestState
import kotlinx.coroutines.launch

class HomeViewModel() : ViewModel() {
    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)

    init {
        observerAllDiaries()

    }

    private fun observerAllDiaries() {
        viewModelScope.launch {
            try {
                MongoDB.getAllDiaries().collect() { result ->
                    diaries.value = result
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching diaries", e)
            }
        }
    }
}