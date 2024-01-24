package com.example.wordnotes.data.model

import android.os.Parcel
import android.os.Parcelable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    val id: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val gender: Int = -1,
    val dob: Long = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        username = parcel.readString() ?: "",
        imageUrl = parcel.readString() ?: "",
        email = parcel.readString() ?: "",
        password = parcel.readString() ?: "",
        phone = parcel.readString() ?: "",
        gender = parcel.readInt(),
        dob = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.apply {
            writeString(id)
            writeString(username)
            writeString(imageUrl)
            writeString(email)
            writeString(password)
            writeString(phone)
            writeInt(gender)
            writeLong(dob)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}

fun User.getFormattedDob(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dob))