package com.kevin.curritos

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx3.rxQuery
import com.kevin.curritos.model.Business
import com.kevin.curritos.model.SearchException
import com.kevin.curritos.model.convert
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

interface SearchRepository {
    fun search(
        radius: Double,
        latitude: Double,
        longitude: Double,
        offset: Int,
        term: String
    ): Observable<List<Business>>

    fun searchZip(
        radius: Double,
        zip: String,
        offset: Int,
        term: String
    ): Observable<List<Business>>
}

class SearchRepositoryImp @Inject constructor(private val apolloClient: ApolloClient) :
    SearchRepository {

    override fun search(
        radius: Double,
        latitude: Double,
        longitude: Double,
        offset: Int,
        term: String
    ): Observable<List<Business>> {
        return apolloClient.rxQuery(SearchYelpQuery(radius, latitude, longitude, offset, term))
            .map { response ->
                if (!response.hasErrors()) {
                    val businesses =
                        response.data?.search?.business?.filterNotNull()?.map { it.convert() }
                    businesses ?: listOf()
                } else {
                    val errorMessage = response.errors?.first()?.message ?: "Unknown Error"
                    val isLocationError =
                        response.errors?.firstOrNull()?.message?.contains("location") ?: false
                    throw SearchException(errorMessage, isLocationError)
                }
            }
    }

    override fun searchZip(
        radius: Double,
        zip: String,
        offset: Int,
        term: String
    ): Observable<List<Business>> {
        return apolloClient.rxQuery(SearchYelpZipQuery(radius, zip, offset, term))
            .map { response ->
                if (!response.hasErrors()) {
                    val businesses =
                        response.data?.search?.business?.filterNotNull()?.map { it.convert() }
                    businesses ?: listOf()
                } else {
                    val errorMessage = response.errors?.first()?.message ?: "Unknown Error"
                    val isLocationError =
                        response.errors?.firstOrNull()?.message?.contains("location") ?: false
                    throw SearchException(errorMessage, isLocationError)
                }
            }
    }
}