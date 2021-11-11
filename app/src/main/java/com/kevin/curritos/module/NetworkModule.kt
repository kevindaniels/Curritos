package com.kevin.curritos.module

import com.apollographql.apollo.ApolloClient
import com.kevin.curritos.SearchRepository
import com.kevin.curritos.SearchRepositoryImp
import com.kevin.curritos.network.AuthInterceptor
import com.kevin.curritos.network.YelpApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    const val ENDPOINT_KEY = "ENDPOINT_KEY"

    @Provides
    @Singleton
    fun providesApolloClient(authInterceptor: AuthInterceptor,  @Named(ENDPOINT_KEY) endpoint: String): ApolloClient {
        return YelpApi(authInterceptor, endpoint).getApolloClient()
    }

    @Provides
    @Singleton
    fun providesSearchRepository(apolloClient: ApolloClient): SearchRepository {
        return SearchRepositoryImp(apolloClient)
    }

    @Provides
    @Singleton
    @Named(ENDPOINT_KEY)
    fun providesBaseUrl(): String = YelpApi.YELP_API_ENDPOINT
}