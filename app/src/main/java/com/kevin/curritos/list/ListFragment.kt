package com.kevin.curritos.list

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnPreDraw
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.kevin.curritos.R
import com.kevin.curritos.base.AbstractBaseFragment
import com.kevin.curritos.base.Logger
import com.kevin.curritos.databinding.FragmentListBinding
import com.kevin.curritos.model.Business
import com.kevin.curritos.model.SearchException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : AbstractBaseFragment<ListViewModel, FragmentListBinding>(),
    BusinessAdapter.BusinessClickListener {

    override val viewModel: ListViewModel by viewModels()
    override val layoutResId = R.layout.fragment_list
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentListBinding =
        FragmentListBinding::inflate
    private val args: ListFragmentArgs by navArgs()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val businessAdapter = BusinessAdapter(arrayListOf(), this)
    private var cancellationSource: CancellationTokenSource? = null
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private var isSearching = false
    private var lastSearch = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setUpClickListeners() {
        binding.searchButton.apply {
            setOnClickListener {
                doZipSearch(binding.zipEditText.text.toString())
            }
        }
    }

    private fun doZipSearch(zip: String) {
        binding.searchButton.isEnabled = false
        lastSearch = zip
        setLoading(true)
        viewModel.searchZip(zip)
        clearKeyboard()
    }

    override fun setUpViews() {
        binding.apply {
            searchButton.isEnabled = false
            recyclerView.adapter = businessAdapter
        }

        if (args.deniedLocation) {
            (view?.parent as ViewGroup).doOnPreDraw {
                startPostponedEnterTransition()
            }
            setUpZipSearch()
            setLoading(false)
        }
    }

    // Suppressing MissingPermission here because after updating Android Studio, I'm getting an error
    // https://stackoverflow.com/questions/62533060/missing-permissions-required-by-fusedlocationproviderclient-getlastlocation-and
    @SuppressLint("MissingPermission")
    override fun callViewModel() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (viewModel.businessesLiveData.value != null) return
            setLoading(true)
            cancellationSource = CancellationTokenSource().apply {
                // We try to use the last location first since it is much faster and uses less
                // resources. If we don't get the last location we try to get the current location.
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location == null) {
                            // CancellationTokenSource's token is being passed
                            getCurrentLocation(token)
                        } else {
                            locationReceived(location.latitude, location.longitude)
                        }
                    }.addOnFailureListener { exception ->
                        Logger.e("Error getting last location", exception)
                        getCurrentLocation(token)
                    }
            }
        } else if (!args.deniedLocation) {
            setUpZipSearch()
        }
    }

    override fun startObserving() {
        viewModel.businessesLiveData.observe(viewLifecycleOwner, { businesses ->
            setLoading(false)
            val error = businesses.error
            when {
                error != null -> handleError(error)
                businesses.isEmpty() -> handleEmpty()
                else -> handleResults(businesses.businesses)
            }
            isSearching = false
        })
    }

    override fun onResume() {
        super.onResume()
        setActionBarTitle(R.string.app_name)
    }

    override fun onStop() {
        super.onStop()
        cancellationSource?.cancel()
        cancellationSource = null
    }

    override fun onBusinessClicked(businessImage: ImageView, business: Business) {
        val extras =
            FragmentNavigatorExtras(businessImage to businessImage.transitionName)
        if (!args.deniedLocation) {
            businessImage.viewTreeObserver
                .addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
        }
        findNavController().navigate(
            ListFragmentDirections.actionListFragmentToDetailsFragment(
                business = business,
                latitude = userLatitude?.toString(),
                longitude = userLongitude?.toString()
            ), extras
        )
    }

    // We suppress MissingPermission here since we only call this shortly after previously checking
    // the permission in the calling function.
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(token: CancellationToken) {
        fusedLocationClient.getCurrentLocation(
            // Balanced is ~100m accuracy which is fine for getting restaurants in a
            // 12 mile range
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
            token
        ).addOnSuccessListener { location: Location? ->
            if (location == null) {
                locationErrorHandler()
            } else {
                locationReceived(location.latitude, location.longitude)
            }
        }.addOnFailureListener { exception ->
            Logger.e("Error getting current location", exception)
            locationErrorHandler()
        }
    }

    private fun setUpZipSearch() {
        binding.apply {
            zipGroup.visibility = View.VISIBLE
            zipEditText.apply {
                addTextChangedListener { text ->
                    searchButton.isEnabled = text?.length == 5 && text.toString() != lastSearch

                }
                setOnKeyListener { _, keyCode, keyEvent: KeyEvent? ->
                    val isEnterKey =
                        keyEvent?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER
                    val zip = binding.zipEditText.text.toString()
                    if (isEnterKey && zip.length == 5 && zip != lastSearch) {
                        doZipSearch(zip)
                        true
                    } else {
                        if (isEnterKey) {
                            makeSnackBar(R.string.list_search_invalid_zip)
                        }
                        false
                    }
                }
            }
        }
    }

    private fun locationErrorHandler() {
        setLoading(false)
        makeSnackBar(R.string.list_location_error)
        setUpZipSearch()
    }

    private fun locationReceived(lat: Double, long: Double) {
        userLatitude = lat
        userLongitude = long
        viewModel.search(lat, long)
    }

    private fun handleError(throwable: Throwable) {
        Logger.d("Got error")
        binding.apply {
            recyclerView.visibility = View.GONE
            messageText.apply {
                text = if (throwable is SearchException && throwable.isLocationNotFound) {
                    resources.getString(R.string.list_search_location_error)
                } else {
                    resources.getString(R.string.list_search_generic_error)
                }
                visibility = View.VISIBLE
            }
        }
    }

    private fun handleEmpty() {
        Logger.d("Got no results")
        binding.apply {
            recyclerView.visibility = View.GONE
            messageText.apply {
                text = resources.getString(R.string.list_search_no_results)
                visibility = View.VISIBLE
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.apply {
                recyclerView.visibility = View.GONE
                messageText.visibility = View.GONE
            }
        }
        binding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun handleResults(businesses: List<Business>) {
        binding.recyclerView.visibility = View.VISIBLE
        Logger.d("Got ${businesses.size} results")
        businessAdapter.updateData(businesses)
    }
}

