package com.kevin.curritos.base

import android.app.Application
import android.os.StrictMode
import com.kevin.curritos.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.*
import android.os.StrictMode.VmPolicy
import java.lang.RuntimeException


@HiltAndroidApp
class CurrApplication : Application() {
//    init {
//        if(BuildConfig.DEBUG) {
//            StrictMode.enableDefaults()
//            StrictMode.setVmPolicy(
//                VmPolicy.Builder()
//                    .detectLeakedClosableObjects()
//                    .penaltyLog()
//                    .build()
//            )
//        }
//
//        try {
//            Class.forName("dalvik.system.CloseGuard")
//                .getMethod("setEnabled", Boolean::class.javaPrimitiveType)
//                .invoke(null, true)
//        } catch (e: ReflectiveOperationException) {
//            throw RuntimeException(e)
//        }
//    }
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}