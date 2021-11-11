package com.kevin.curritos

import android.os.StrictMode
import com.apollographql.apollo.ApolloClient
import com.kevin.curritos.network.AuthInterceptor
import com.kevin.curritos.network.YelpApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.mockwebserver.MockWebServer
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestNetworkModule {

    var server = MockWebServer()
    var port = 0
    var url = ""
    private const val ENDPOINT_KEY = "ENDPOINT_KEY"

    @Provides
    @Singleton
    fun providesApolloClient(authInterceptor: AuthInterceptor, @Named(ENDPOINT_KEY) endpoint: String): ApolloClient {
        return YelpApi(authInterceptor, endpoint).getApolloClient()
    }

    @Provides
    @Singleton
    fun providesSearchRepository(apolloClient: ApolloClient): SearchRepository {
        return SearchRepositoryImp(apolloClient)
    }

    // cluster fuck I know... ran out of time and couldn't figure out how to get the server's
    // url to inject into YelpAPI on a background thread. this works for now, but given more time
    // would find a better solution.
    @Provides
    @Named(ENDPOINT_KEY)
    fun providesBaseUrl(): String {
        if(url.isNotBlank()) return  url
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        server.start()
        url = server.url(path = "/v3/graphql").toString()
        val portSB = StringBuilder()
        var foundLetter = false
        url.replace("http://localhost:", "").forEach { char ->
            if(char.isDigit() && !foundLetter) {
                portSB.append(char)
            } else {
                foundLetter = true
            }
        }
        server.shutdown()
        port = portSB.toString().toInt()
        return url
    }
}