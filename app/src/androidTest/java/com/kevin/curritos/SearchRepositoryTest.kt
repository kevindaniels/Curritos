package com.kevin.curritos

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apollographql.apollo.exception.ApolloHttpException
import com.kevin.curritos.base.Logger
import com.kevin.curritos.model.SearchException
import com.kevin.curritos.network.AuthInterceptor
import com.kevin.curritos.network.YelpApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchRepositoryTest {

    private var server = MockWebServer()
    lateinit var searchRepository: SearchRepository

    @Before
    fun setUp() {
        server.start()
        val url = server.url(path = "/v3/graphql")
        Logger.d("URL: $url")
        val yelpApi = YelpApi(AuthInterceptor(), url.toString())
        searchRepository = SearchRepositoryImp(yelpApi.getApolloClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun test_response_with_results() {
        server.enqueue(MockResponse().setBody(getAssetFileContents("response.json")))
        searchRepository.search(0.0, 0.0, 0.0, 0, "")
            .test()
            .await()
            .assertValue { businesses ->
                assertEquals(50, businesses.size)
                val business = businesses.first()
                assertEquals("8A7qOO6r1ZpZwVaYndA6-A", business.id)
                assertEquals("Electric Burrito", business.name)
                assertEquals(4.5, business.rating, 0.0)
                assertEquals(
                    "https://s3-media2.fl.yelpcdn.com/bphoto/ip4kF9bDNEoTTFCP2LVoPA/o.jpg",
                    business.photoUrl
                )
                assertEquals("$$", business.price)
                assertEquals("", business.phoneNumber)
                assertEquals(40.727858, business.latitude)
                assertEquals(-73.985473, business.longitude)
                assertEquals("81 St Marks Pl", business.address)
                assert(businesses.isNotEmpty())
                true
            }
    }

    @Test
    fun test_zip_response_with_results() {
        server.enqueue(MockResponse().setBody(getAssetFileContents("response.json")))
        searchRepository.searchZip(0.0, "", 0, "")
            .test()
            .await()
            .assertValue { businesses ->
                assertEquals(50, businesses.size)
                val business = businesses.first()
                assertEquals("8A7qOO6r1ZpZwVaYndA6-A", business.id)
                assertEquals("Electric Burrito", business.name)
                assertEquals(4.5, business.rating, 0.0)
                assertEquals(
                    "https://s3-media2.fl.yelpcdn.com/bphoto/ip4kF9bDNEoTTFCP2LVoPA/o.jpg",
                    business.photoUrl
                )
                assertEquals("$$", business.price)
                assertEquals("", business.phoneNumber)
                assertEquals(40.727858, business.latitude)
                assertEquals(-73.985473, business.longitude)
                assertEquals("81 St Marks Pl", business.address)
                assert(businesses.isNotEmpty())
                true
            }
    }

    @Test
    fun test_no_results() {
        server.enqueue(MockResponse().setBody(getAssetFileContents("response_empty.json")))
        searchRepository.search(0.0, 0.0, 0.0, 0, "")
            .test()
            .await()
            .assertValue { businesses ->
                assertEquals(0, businesses.size)
                true
            }
    }

    @Test
    fun test_zip_no_results() {
        server.enqueue(MockResponse().setBody(getAssetFileContents("response_empty.json")))
        searchRepository.searchZip(0.0, "", 0, "")
            .test()
            .await()
            .assertValue { businesses ->
                assertEquals(0, businesses.size)
                true
            }
    }

    @Test
    fun test_location_error() {
        server.enqueue(MockResponse().setBody(getAssetFileContents("response_location_error.json")))
        searchRepository.search(0.0, 0.0, 0.0, 0, "")
            .test()
            .await()
            .assertError { throwable ->
                assert((throwable as SearchException).isLocationNotFound)
                assertEquals(
                    "Could not execute search, try specifying a more exact location.",
                    throwable.message
                )
                true
            }
    }

    @Test
    fun test_zip_location_error() {
        server.enqueue(MockResponse().setBody(getAssetFileContents("response_location_error.json")))
        searchRepository.searchZip(0.0, "", 0, "")
            .test()
            .await()
            .assertError { throwable ->
                assert((throwable as SearchException).isLocationNotFound)
                assertEquals(
                    "Could not execute search, try specifying a more exact location.",
                    throwable.message
                )
                true
            }
    }

    @Test
    fun test_auth_error() {
        server.enqueue(MockResponse().setResponseCode(401))
        searchRepository.search(0.0, 0.0, 0.0, 0, "")
            .test()
            .await()
            .assertError { throwable ->
                assert(throwable is ApolloHttpException)
                assertEquals(
                    "HTTP 401 Client Error",
                    throwable.message
                )
                true
            }
    }

    @Test
    fun test_zip_auth_error() {
        server.enqueue(MockResponse().setResponseCode(401))
        searchRepository.searchZip(0.0, "", 0, "")
            .test()
            .await()
            .assertError { throwable ->
                assert(throwable is ApolloHttpException)
                assertEquals(
                    "HTTP 401 Client Error",
                    throwable.message
                )
                true
            }
    }
}