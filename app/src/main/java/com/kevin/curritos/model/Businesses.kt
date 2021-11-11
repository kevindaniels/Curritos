package com.kevin.curritos.model

data class Businesses(
    val businesses: List<Business>,
    val error: Throwable? = null
) {

    fun isEmpty() = businesses.isEmpty()

    fun hasError() = error != null

    fun isLocationError(): Boolean {
        return error != null && error is SearchException && error.isLocationNotFound
    }

    fun errorMessage() = error?.message
}