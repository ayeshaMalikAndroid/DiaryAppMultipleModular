package com.example.diaryapp.model

import com.example.diaryapp.utils.toRealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.time.Instant


    open class Diary : RealmObject {
        @PrimaryKey
        var _id: ObjectId = ObjectId.invoke()
        var ownerId: String = ""
        var mood: String = Mood.Neutral.name
        var title: String = ""
        var description: String = ""
        var images: RealmList<String> = realmListOf()
        var date: RealmInstant = Instant.now().toRealmInstant()
    }
















//open class Diary : RealmObject {
/*  _id is not the type of integer but,it'll be an object Id
this objectID is a part of our
  Realm SDK and it will generate or it'll
  represent a unique ID for each and every object of our diary.*/
//   @PrimaryKey // this annotation will mark our field as a primary key inside the realm.
// var _id: io.realm.kotlin.types.ObjectId = io.realm.kotlin.types.ObjectId.create()
//  var _id: ObjectId = ObjectId.invoke()
/* 1-owner ID will actually be a unique identifier of
 each and every authenticated user in our mongoDB
 2-By using this owner ID, we will be able to fetch only
 the diaries that are associated with our actual user.

 */



//}


// Schema data
/*
{"name" :"Diary",
"bsonType" : "object",
"required" : [
   "_id",
   "ownerId",
   "title",
   "description",
   "mood",
   "date"
    ],
    "properties" :{
        "_id": {
            "bsonType" :"objectId"
        },
        "ownerId": {
            "bsonType" :"string"
        },
         "title": {
            "bsonType" :"string"
        },
         "description": {
            "bsonType" :"string"
        },
         "mood": {
            "bsonType" :"string"
        },
        "newImages" : {
            "bsonType" :"array",
            "items" :{
                "bsonType" :"string"
            }
        },
         "date": {
            "bsonType" :"date"
        }
    }
}
 */