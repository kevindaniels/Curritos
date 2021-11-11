package com.kevin.curritos.model

import android.os.Parcel
import android.os.Parcelable
import com.kevin.curritos.SearchYelpQuery
import com.kevin.curritos.SearchYelpZipQuery

data class Business(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val rating: Double,
    val price: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    var photoUrl: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(phoneNumber)
        parcel.writeDouble(rating)
        parcel.writeString(price)
        parcel.writeString(address)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeString(photoUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Business> {
        override fun createFromParcel(parcel: Parcel): Business {
            return Business(parcel)
        }

        override fun newArray(size: Int): Array<Business?> {
            return arrayOfNulls(size)
        }
    }

    fun hasCoordinates(): Boolean {
        return longitude != null && latitude != null
    }
}

fun SearchYelpQuery.Business.convert(): Business {
    return Business(
            id = id ?: "",
            name = name ?: "",
            phoneNumber = display_phone ?: "",
            rating = rating ?: -1.0,
            price = price ?: "",
            address = location?.address1 ?: "",
            latitude = coordinates?.latitude,
            longitude = coordinates?.longitude,
            photoUrl = photos?.firstOrNull()
    )
}

fun SearchYelpZipQuery.Business.convert(): Business {
    return Business(
            id = id ?: "",
            name = name ?: "",
            phoneNumber = display_phone ?: "",
            rating = rating ?: -1.0,
            price = price ?: "",
            address = location?.address1 ?: "",
            latitude = coordinates?.latitude,
            longitude = coordinates?.longitude,
            photoUrl = photos?.firstOrNull()
    )
}