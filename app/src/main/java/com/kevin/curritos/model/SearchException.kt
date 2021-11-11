package com.kevin.curritos.model

class SearchException(override val message: String?, val isLocationNotFound: Boolean) : Exception()