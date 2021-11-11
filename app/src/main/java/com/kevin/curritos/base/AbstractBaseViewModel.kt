package com.kevin.curritos.base

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

abstract class AbstractBaseViewModel : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    fun hold(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun dispose() {
        compositeDisposable.dispose()
    }

    open fun clear() {
        compositeDisposable.clear()
    }
}