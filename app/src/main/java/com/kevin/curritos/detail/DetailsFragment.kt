package com.kevin.curritos.detail

import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.kevin.curritos.R
import com.kevin.curritos.base.AbstractBaseFragment
import com.kevin.curritos.databinding.FragmentDetailsBinding
import com.kevin.curritos.model.Business
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.kevin.curritos.base.Logger
import java.lang.Exception


class DetailsFragment : AbstractBaseFragment<DetailsViewModel, FragmentDetailsBinding>(),
    OnMapReadyCallback {

    companion object {
        private const val ANIMATION_DURATION = 275L
    }

    override val layoutResId = R.layout.fragment_details
    override val viewModel: DetailsViewModel by viewModels()
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDetailsBinding =
        FragmentDetailsBinding::inflate

    private val args: DetailsFragmentArgs by navArgs()
    private lateinit var business: Business
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.image_move).apply {
                duration = ANIMATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
            }
        sharedElementReturnTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.image_move).apply {
                duration = ANIMATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
            }

        business = args.business
        try {
            userLatitude = args.latitude?.toDouble()
            userLongitude = args.longitude?.toDouble()
        } catch (e: Exception) {
            Logger.e("Expecting a string of a Double", e)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setUpViews() {
        binding.businessDetailsImage.transitionName = business.id

        setActionBarTitle(business.name)

        if(business.photoUrl != null) {
            Glide.with(this)
                .load(args.business.photoUrl)
                .into(binding.businessDetailsImage)
        } else {
            binding.businessDetailsImage.setImageResource(R.drawable.ic_burrito_man)
        }

        val address = business.address
        binding.businessAddress.apply {
            if (address.isNotBlank()) {
                text = address
                visibility = View.VISIBLE
            } else {
                visibility = View.INVISIBLE
            }
        }

        // Price/rating
        val price = business.price
        val rating = business.rating
        val priceAndRatingsText = when {
            price.isNotBlank() && rating >= 0 -> "${business.price} • ${business.rating}"
            rating >= 0 -> rating.toString()
            price.isNotBlank() -> price
            else -> ""
        }
        binding.businessPriceAndRating.text = priceAndRatingsText

        // Phone number and price/rating margin
        val phoneNumber = business.phoneNumber
        binding.businessPhoneNumber.apply {
            val ratingsLayoutParams: ViewGroup.MarginLayoutParams =
                binding.businessPriceAndRating.layoutParams as ViewGroup.MarginLayoutParams
            ratingsLayoutParams.marginStart = if (phoneNumber.isNotBlank()) {
                text = phoneNumber
                visibility = View.VISIBLE
                Linkify.addLinks(this, Linkify.PHONE_NUMBERS)
                resources.getDimensionPixelSize(R.dimen.business_margin)
            } else {
                visibility = View.GONE
                0
            }
        }

        if (!business.hasCoordinates()) {
            makeSnackBar(R.string.list_location_error)
            return
        }

        val mapFragment =
            binding.mapFragment.findFragment<DetailsFragment>().childFragmentManager.fragments.firstOrNull()
        if (mapFragment != null && mapFragment is SupportMapFragment) {
            mapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        val businessLocation = LatLng(business.latitude!!, business.longitude!!)
        map.apply {
            // business marker
            addMarker(
                MarkerOptions()
                    .position(businessLocation)
                    .title(business.name)
            )

            // User location
            userLatitude?.let { lat ->
                userLongitude?.let { long ->
                    addCircle(
                        CircleOptions()
                            .fillColor(requireContext().getColor(R.color.blue))
                            .strokeColor(requireContext().getColor(R.color.white))
                            .strokeWidth(0.1f)
                            .radius(45.0)
                            .center(LatLng(lat, long))
                    )
                }
            }

            // Show business on map
            moveCamera(CameraUpdateFactory.newLatLngZoom(businessLocation, 16f))
        }
    }
}
