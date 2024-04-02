@file:Suppress("DEPRECATION")

package com.example.diaryapp.data.repository

import android.security.keystore.UserNotAuthenticatedException
import android.util.Log
import androidx.room.util.query
import com.example.diaryapp.model.Diary
import com.example.diaryapp.utils.Constants.APP_ID
import com.example.diaryapp.utils.RequestState
import com.example.diaryapp.utils.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.subscriptions
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId
import io.realm.kotlin.internal.platform.*
import org.mongodb.kbson.ObjectId

//how to synchronize the data between the client and the server and in our case Android application and MongoDB Atlas.
object MongoDB : MongoRepository {
    private val app = App.Companion.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureTheRealm()
    }

    override fun configureTheRealm() {
        if (user != null) {
            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query<Diary>(query = "ownerId == $0", user.id),
                        name = "User's Diaries"
                    )
                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)

            // Refreshing subscriptions after configuring the Realm instance
            realm.subscriptions.refresh()
        }
    }

    //  lecture 33 recall....
    override fun getAllDiaries(): Flow<Diaries> {
        return if (user != null) {
            try {
                realm.query<Diary>(query = "ownerId == $0", user.id)

                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map { result ->
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(com.example.diaryapp.data.repository.UserNotAuthenticatedException())) }
        }
    }

    override fun getSelectedDiary(diaryId: ObjectId): Flow<RequestState<Diary>> {
        return if (user != null) {
            try {
                realm.query<Diary>(query = "_id == $0", diaryId).asFlow().map {
                    RequestState.Success(data = it.list.first())
                }

            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(com.example.diaryapp.data.repository.UserNotAuthenticatedException())) }

        }
    }

    override suspend fun insertDiary(diary: Diary): RequestState<Diary> {
        return if (user != null) {
            realm.write {
                try {
                    val addedDiary = copyToRealm(diary.apply { ownerId = user.identity })
                    RequestState.Success(data = addedDiary)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } else {
            RequestState.Error(com.example.diaryapp.data.repository.UserNotAuthenticatedException())
        }
    }

    override suspend fun updateDiary(diary: Diary): RequestState<Diary> {
        return if (user != null) {
            realm.write {
                val queriedDiary = query<Diary>(query = "_id == $0", diary._id).first().find()
                if (queriedDiary != null) {
                    queriedDiary.title = diary.title
                    queriedDiary.description = diary.description
                    queriedDiary.mood = diary.mood
                    queriedDiary.images = diary.images
                    queriedDiary.date = diary.date
                    RequestState.Success(data = queriedDiary)
                } else {
                    RequestState.Error(error = Exception("Queried Diary does not exist."))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteDiary(id: ObjectId): RequestState<Diary> {
        return if (user != null) {
            realm.write {
                val diary =
                    query<Diary>(query = "_id == $0 AND ownerId == $1", id, user.id)
                        .first().find()
                if (diary != null) {
                    try {
                        delete(diary)
                        RequestState.Success(data = diary)
                    } catch (e: Exception) {
                        RequestState.Error(e)
                    }
                } else {
                    RequestState.Error(Exception("Diary does not exist."))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }
}



private class UserNotAuthenticatedException : Exception("User is not Logged in.")
/*
* single diary collection in the database where diaries from all users will be stored.
*Only want to observe the diaries that were created by the current authenticated user.
* add data using the Realm Sync SDK, that data will be stored locally in the realm db on Android studio.
* */


// after this realm configured will be used to interact with Mongodb to add,update or delete
//    override fun configureTheRealm() {
//        if (user != null) {
//            /*           First parameter is the actual user or our current
//            currently authenticated user from a MongoDB.
//            Second parameter will be the actual class or multiple classes.
//            going to sync with our mongodb realm or MongoDB Atlas.
//  */
//            /*Synchronization will basically be used to setup realm DB data
//            can be synchronized between devices using the Atlas device sync.
//            */
//            // need authenticated user that we need to pass to this builder.
//
//            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
//                .initialSubscriptions { sub ->
//                    add(
//                        query = sub.query<Diary>(query = "ownerId == $0", user.id),
//                        name = "User's Diaries"
//                    )
//                }
//                .log(LogLevel.ALL)
//                .build()
//            realm = Realm.open(config)
//        }
//    }