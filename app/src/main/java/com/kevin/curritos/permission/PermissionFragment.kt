package com.kevin.curritos.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.kevin.curritos.R
import com.kevin.curritos.base.AbstractBaseFragment
import com.kevin.curritos.base.EmptyViewModel
import com.kevin.curritos.base.Logger
import com.kevin.curritos.databinding.FragmentPermissionBinding

class PermissionFragment : AbstractBaseFragment<EmptyViewModel, FragmentPermissionBinding>() {

    override val layoutResId = R.layout.fragment_permission
    override val viewModel: EmptyViewModel by viewModels()
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPermissionBinding =
        FragmentPermissionBinding::inflate

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    Logger.d("Fine location granted")
                    navigateToListFragment()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    Logger.d("Coarse location granted")
                    navigateToListFragment()
                }
                else -> {
                    // No location access granted.
                    Logger.d("No location granted")
                    makeSnackBar(R.string.permission_toast_message)
                }
            }
        }
    }

    override fun setUpViews() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                Logger.d("Fine location already granted")
                navigateToListFragment()
            }
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                // You can use the API that requires the permission.
                Logger.d("Coarse location already granted")
                navigateToListFragment()
            }
            else -> {
                binding.root.visibility = View.VISIBLE
            }
        }
    }

    override fun setUpClickListeners() {
        binding.enableButton.setOnClickListener {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        binding.useZipButton.setOnClickListener {
            navigateToListFragment(true)
        }
    }

    private fun navigateToListFragment(deniedLocation: Boolean = false) {
        findNavController().navigate(PermissionFragmentDirections.actionPermissionFragmentToListFragment(deniedLocation))
    }
}