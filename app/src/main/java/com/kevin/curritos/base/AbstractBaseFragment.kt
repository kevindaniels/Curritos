package com.kevin.curritos.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.kevin.curritos.MainActivity
import com.kevin.curritos.R
import java.lang.Exception

abstract class AbstractBaseFragment<VM : AbstractBaseViewModel, VB : ViewBinding> : Fragment() {

    protected abstract val layoutResId: Int
    abstract val viewModel: VM
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

    fun setActionBarTitle(title: String) {
        try {
            (requireActivity() as MainActivity).supportActionBar?.title = title
        } catch (e: Exception) {
            Logger.e("Couldn't set title", e)
        }
    }

    fun setActionBarTitle(@StringRes titleRed: Int) {
        setActionBarTitle(resources.getString(titleRed))
    }

    fun makeSnackBar(@StringRes resource: Int) {
        Snackbar.make(
            binding.root,
            resource,
            Snackbar.LENGTH_LONG
        ).show()
    }

    protected fun clearKeyboard() {
        context?.apply {
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
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