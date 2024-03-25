package com.example.diaryapp.data.repository

import com.example.diaryapp.model.Diary
import com.example.diaryapp.utils.Constants.APP_ID
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration


//how to synchronize the data between the client and the server and in our case Android application and MongoDB Atlas.
object MongoDB : MongoRepository {
    private val app = App.Companion.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm
    // after this realm configured will be used to interact with Mongodb to add,update or delete
    override fun configureTheRealm() {
        if (user != null) {
            /*           First parameter is the actual user or our current
            currently authenticated user from a MongoDB.
            Second parameter will be the actual class or multiple classes.
            going to sync with our mongodb realm or MongoDB Atlas.
  */
            /*Synchronization will basically be used to setup realm DB data
            can be synchronized between devices using the Atlas device sync.
            */
            // need authenticated user that we need to pass to this builder.
            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query("ownerId == $0", user.identity),
                        name = "User's Diaries"
                    )

                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)
        }
    }
}

/*
* single diary collection in the database where diaries from all users will be stored.
*Only want to observe the diaries that were created by the current authenticated user.
* add data using the Realm Sync SDK, that data will be stored locally in the realm db on Android studio.
* */