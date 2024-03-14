package com.example.diaryapp.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.time.Instant

class Diary : RealmObject {
    /*  _id is not the type of integer but,it'll be an object Id
  this objectID is a part of our
      Realm SDK and it will generate or it'll
      represent a unique ID for each and every object of our diary.*/
    @PrimaryKey // this annotation will mark our field as a primary key inside the realm.
    var _id: io.realm.kotlin.types.ObjectId = io.realm.kotlin.types.ObjectId.create()

    /* 1-owner ID will actually be a unique identifier of
     each and every authenticated user in our mongoDB
     2-By using this owner ID, we will be able to fetch only
     the diaries that are associated with our actual user.

     */
    var ownerId: String = ""
    var title: String = ""
        //with the property of name get the name of content  as a string (awful)
    var mood :String = Mood.Neutral.name
    var description: String = ""
    var images: RealmList<String> = realmListOf()
    var date: RealmInstant = RealmInstant.from(System.currentTimeMillis(), 0)


}