package com.example.wordnotes.data.model

import android.os.Parcel
import android.os.Parcelable

data class User(
    val id: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val gender: Int = -1,
    val dob: Long = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        username = parcel.readString() ?: "",
        profileImageUrl = parcel.readString() ?: "",
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
            writeString(profileImageUrl)
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