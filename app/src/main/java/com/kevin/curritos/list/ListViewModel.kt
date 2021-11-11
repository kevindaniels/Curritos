package com.kevin.curritos.list

import androidx.lifecycle.MutableLiveData
import com.kevin.curritos.SearchRepository
import com.kevin.curritos.base.AbstractBaseViewModel
import com.kevin.curritos.base.Logger
import com.kevin.curritos.model.Businesses
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(private val searchRepository: SearchRepository) :
    AbstractBaseViewModel() {

    companion object {
        private const val BURRITO_SEARCH_TERM = "burrito"
        private const val TWELVE_MILES_RADIUS = 19312.1
    }

    val businessesLiveData = MutableLiveData<Businesses>()

    fun search(latitude: Double, longitude: Double, searchTerm: String = BURRITO_SEARCH_TERM) {
        // NYC
//        val lat = 40.679430
//        val long = -73.998300
        searchRepository.search(TWELVE_MILES_RADIUS, latitude, longitude, 0, searchTerm)
            .subscribeOn(Schedulers.io())
            .subscribe({ businesses ->
                businessesLiveData.postValue(Businesses(businesses))
            }, { throwable ->
                Logger.e("Error searching businesses by coordinates", throwable)
                businessesLiveData.postValue(Businesses(listOf(), throwable))
            })
    }

    fun searchZip(zip: String, searchTerm: String = BURRITO_SEARCH_TERM) {
        searchRepository.searchZip(TWELVE_MILES_RADIUS, zip, 0, searchTerm)
            .subscribeOn(Schedulers.io())
            .subscribe({ businesses ->
                businessesLiveData.postValue(Businesses(businesses))
            }, { throwable ->
                Logger.e("Error searching businesses by zip", throwable)
                businessesLiveData.postValue(Businesses(listOf(), throwable))
            })
    }
}