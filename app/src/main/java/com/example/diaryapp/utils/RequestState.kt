package com.example.diaryapp.utils

sealed class RequestState<out T>{
    object Loading : RequestState<Nothing>()
    object Idle :RequestState<Nothing>()
    data class Success<out  T>(val data :T) :RequestState<T>()
    data class Error(val error :Throwable) :RequestState<Nothing>()

}
