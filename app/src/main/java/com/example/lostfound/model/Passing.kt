package com.example.lostfound.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Passing(val userName: String,val profileUri:String,val userid:String,val chatId:String) : Parcelable{
    constructor():this("","","",""){

    }
}
