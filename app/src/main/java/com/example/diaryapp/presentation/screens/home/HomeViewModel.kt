package com.example.diaryapp.presentation.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.connectivity.ConnectivityObserver
import com.example.util.connectivity.NetworkConnectivityObserver
import com.example.diaryapp.data.database.ImageToDeleteDao
import com.example.diaryapp.data.database.entity.ImageToDelete
import com.example.diaryapp.data.repository.Diaries
import com.example.diaryapp.data.repository.MongoDB
import com.example.util.model.RequestState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectivity: NetworkConnectivityObserver,
    private val imageToDeleteDao: ImageToDeleteDao
) : ViewModel() {
    private lateinit var allDiariesJob: Job
    private lateinit var filteredDiariesJob: Job
    private var network by mutableStateOf(ConnectivityObserver.Status.Unavailable)
    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)
    var dateIsSelected by mutableStateOf(false)
        private set

    init {
        getDiaries()
        viewModelScope.launch {
            //actual connectivity status of the network changes, then our network variable will be updated.
            connectivity.observe().collect() {
                network = it
            }
        }
    }

    fun getDiaries(zonedDateTime: ZonedDateTime? = null) {
        dateIsSelected = zonedDateTime != null
        diaries.value = RequestState.Loading
        if (dateIsSelected && zonedDateTime != null) {
            observeFilteredDiaries(zonedDateTime = zonedDateTime)
        } else {
            observerAllDiaries()
        }
    }

    private fun observerAllDiaries() {
        allDiariesJob = viewModelScope.launch {
            //when want to observer only the filter diaries then cancel the other job
            if (::filteredDiariesJob.isInitialized) {
                filteredDiariesJob.cancelAndJoin()
            }
            MongoDB.getAllDiaries().collect { result ->
                diaries.value = result
            }

        }
    }

    private fun observeFilteredDiaries(zonedDateTime: ZonedDateTime) {
        filteredDiariesJob = viewModelScope.launch {
            // if try to cancel this job that is not initialized may get an exception
            if (::allDiariesJob.isInitialized) {
                allDiariesJob.cancelAndJoin()
            }

            MongoDB.getFilteredDiaries(zonedDateTime = zonedDateTime).collect() { result ->

                diaries.value = result
            }
        }
    }

    fun deleteAllDiaries(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (network == ConnectivityObserver.Status.Available) {
            //need this identifier to pinpoint the actual directory inside the Firebase storage where storing images.
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            //same directory path already in Firebase storage
            val imagesDirectory = "images/${userId}"
            val storage = FirebaseStorage.getInstance().reference
            storage.child(imagesDirectory)
                .listAll()

                /*  for each and every images from our Firebase Storage
this is location in directory imagesDirectory = "images/${userId}"
the call this delete function to actually delete that images from firebase.
And if failed to delete one of those images,then that images will be
persisted within our images to delete database table so that we can later check
inside our main activity and delete them again.
                 */

                .addOnSuccessListener {
                    it.items.forEach { ref ->
                        val imagePath = "images/${userId}/${ref.name}"
                        storage.child(imagePath).delete()
                            .addOnFailureListener {
                                //observe when our images has failed to delete
                                viewModelScope.launch(Dispatchers.IO) {
                                    imageToDeleteDao.addImageToDelete(
                                        ImageToDelete(remoteImagePath = imagePath)
                                    )

                                }
                            }

                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        val result = MongoDB.deleteAllDiaries()
                        if (result is RequestState.Success) {
                            withContext(Dispatchers.Main) {
                                onSuccess
                            }
                        } else if (result is RequestState.Error) {
                            withContext(Dispatchers.Main) {
                                onError(result.error)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    onError(it)
                }
        } else {
            onError(Exception("No Internet Connectivity."))
        }
    }
}