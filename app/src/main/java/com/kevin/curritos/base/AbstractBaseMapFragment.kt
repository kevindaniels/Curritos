package com.kevin.curritos.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

abstract class AbstractBaseMapFragment<VM : AbstractBaseViewModel, VB : ViewBinding> :
    SupportMapFragment(), OnMapReadyCallback {

    protected abstract val layoutResId: Int
    protected abstract val viewModel: VM
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    private var _binding: VB? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater.invoke(inflater, container, false)
        return requireNotNull(_binding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews()
        setUpClickListeners()
        startObserving()
        callViewModel()
    }

    override fun onDestroy() {
        viewModel.clear()
        super.onDestroy()
    }

    // override to set up views
    protected open fun setUpViews() {}

    // override to add click listeners in here
    protected open fun setUpClickListeners() {}

    // override to observe live data
    protected open fun startObserving() {}

    // override to tell your view model to do stuff
    protected open fun callViewModel() {}
}