package com.kevin.curritos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.kevin.curritos.list.ListViewModel
import com.kevin.curritos.model.Business
import com.kevin.curritos.model.SearchException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.rxjava3.core.Observable
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks

@RunWith(AndroidJUnit4::class)
class ListViewModelTest {

    companion object {
        private const val GENERIC_ERROR_SEARCH = "generic error"
        private const val LOCATION_ERROR_SEARCH = "location error"
        private const val SUCCESS_RESULTS_SEARCH = "success results"
        private const val SUCCESS_EMPTY_SEARCH = "success empty"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var searchRepository: SearchRepository

    private lateinit var listViewModel: ListViewModel

    @Before
    fun setUp() {
        initMocks(this)
        listViewModel = ListViewModel(searchRepository)
        mockSearchRepository()
    }

    @Test
    fun test_location_error() {
        listViewModel.search(0.0, 0.0, LOCATION_ERROR_SEARCH)
        val businesses = listViewModel.businessesLiveData.getOrAwaitValue()
        assert(businesses.hasError())
        assert(businesses.isEmpty())
        assertEquals("forced location error", businesses.errorMessage())
    }

    @Test
    fun test_location_error_zip() {
        listViewModel.searchZip("", LOCATION_ERROR_SEARCH)
        val businesses = listViewModel.businessesLiveData.getOrAwaitValue()
        assert(businesses.hasError())
        assert(businesses.isEmpty())
        assertEquals("forced location error", businesses.errorMessage())
    }

    @Test
    fun test_generic_error() {
        listViewModel.search(0.0, 0.0, GENERIC_ERROR_SEARCH)
        val businesses = listViewModel.businessesLiveData.getOrAwaitValue()
        assert(businesses.hasError())
        assert(businesses.isEmpty())
        assertEquals("forced generic error", businesses.errorMessage())
    }

    @Test
    fun test_generic_error_zip() {
        listViewModel.searchZip("", GENERIC_ERROR_SEARCH)
        val businesses = listViewModel.businessesLiveData.getOrAwaitValue()
        assert(businesses.hasError())
        assert(businesses.isEmpty())
        assertEquals("forced generic error", businesses.errorMessage())
    }

    @Test
    fun test_success_results() {
        listViewModel.search(0.0, 0.0, SUCCESS_RESULTS_SEARCH)
        val businesses = listViewModel.businessesLiveData.getOrAwaitValue()
        assert(!businesses.hasError())
        assert(!businesses.isEmpty())
        assertNull(businesses.errorMessage())
        assertEquals(10, businesses.businesses.size)
        businesses.businesses.first().apply {
            assertEquals("kt1RyygyhfyexcL7GP0iTQ", id)
            assertEquals("Tacos Y Tortas", name)
            assertEquals("10833 Courthouse Rd", address)
            assertEquals("(540) 538-8358", phoneNumber)
            assertEquals("$$", price)
            assertEquals("https://s3-media2.fl.yelpcdn.com/bphoto/QtQhdz7jBW5snqx5sCnBzQ/o.jpg", photoUrl)
            assertEquals(4.5, rating, 0.0)
            assertEquals(38.25302, latitude ?: -1.0, 0.0)
            assertEquals(-77.50115, longitude ?: -1.0, 0.0)
        }
    }

    @Test
    fun test_success_results_zip() {
        listViewModel.searchZip("", SUCCESS_RESULTS_SEARCH)
        val businesses = listViewModel.businessesLiveData.getOrAwaitValue()
        assert(!businesses.hasError())
        assert(!businesses.isEmpty())
        assertNull(businesses.errorMessage())
        assertEquals(10, businesses.businesses.size)
        businesses.businesses.first().apply {
            assertEquals("kt1RyygyhfyexcL7GP0iTQ", id)
            assertEquals("Tacos Y Tortas", name)
            assertEquals("10833 Courthouse Rd", address)
            assertEquals("(540) 538-8358", phoneNumber)
            assertEquals("$$", price)
            assertEquals("https://s3-media2.fl.yelpcdn.com/bphoto/QtQhdz7jBW5snqx5sCnBzQ/o.jpg", photoUrl)
            assertEquals(4.5, rating, 0.0)
            assertEquals(38.25302, latitude ?: -1.0, 0.0)
            assertEquals(-77.50115, longitude ?: -1.0, 0.0)
        }
    }

    @Test
    fun test_success_empty() {
        listViewModel.search(0.0, 0.0, SUCCESS_EMPTY_SEARCH)
        val businesses = listViewModel.businessesLiveData.getOrAwaitValue()
        assert(!businesses.hasError())
        assert(businesses.isEmpty())
        assertNull(businesses.errorMessage())
    }

    @Test
    fun test_success_empty_zip() {
        listViewModel.searchZip("", SUCCESS_EMPTY_SEARCH)
        val businesses = listViewModel.businessesLiveData.getOrAwaitValue()
        assert(!businesses.hasError())
        assert(businesses.isEmpty())
        assertNull(businesses.errorMessage())
    }

    private fun mockSearchRepository() {
        whenever(searchRepository.search(any(), any(), any(), any(), any()))
            .doAnswer { invocationOnMock ->
                getObservableBasedOnSearchTerm(invocationOnMock.arguments.last() as String)
            }
        whenever(searchRepository.searchZip(any(), any(), any(), any()))
            .doAnswer { invocationOnMock ->
                getObservableBasedOnSearchTerm(invocationOnMock.arguments.last() as String)
            }
    }

    private fun getObservableBasedOnSearchTerm(searchTerm: String): Observable<List<Business>> {
        return when(searchTerm) {
            GENERIC_ERROR_SEARCH -> Observable.error(SearchException("forced generic error", false))
            LOCATION_ERROR_SEARCH -> Observable.error(SearchException("forced location error", true))
            SUCCESS_EMPTY_SEARCH -> Observable.just(listOf())
            SUCCESS_RESULTS_SEARCH -> {
                val json = getAssetFileContents("businesses.json")
                val businesses: List<Business> = Gson().fromJson(json)
                Observable.just(businesses)
            }
            else -> {
                Assert.fail("unknown search term")
                throw IllegalStateException()
            }
        }
    }
}