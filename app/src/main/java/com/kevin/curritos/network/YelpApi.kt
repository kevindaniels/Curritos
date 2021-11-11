package com.kevin.curritos.network

import com.apollographql.apollo.ApolloClient
import com.kevin.curritos.base.Logger
import com.kevin.curritos.module.NetworkModule.ENDPOINT_KEY
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject
import javax.inject.Named

class YelpApi @Inject constructor(
    private val authInterceptor: AuthInterceptor,
    @Named(ENDPOINT_KEY) private val endpoint: String
) {

    companion object {
        const val YELP_API_ENDPOINT = "https://api.yelp.com/v3/graphql"
    }

    fun getApolloClient(): ApolloClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor(Logger).apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        return ApolloClient.builder()
            .serverUrl(endpoint)
            .okHttpClient(okHttpClient)
            .build()
    }
}