package com.kevin.curritos.base

import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

object Logger : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        setTag()
        Timber.i(message)
    }

    @JvmStatic
    @Suppress("Unused")
    fun d(message: String, vararg args: Any?) {
        setTag()
        Timber.d(message, args)
    }

    @JvmStatic
    @Suppress("Unused")
    fun d(throwable: Throwable?) {
        setTag()
        Timber.d(throwable)
    }

    @JvmStatic
    @Suppress("Unused")
    fun e(message: String, throwable: Throwable?) {
        setTag()
        Timber.e(throwable, message)
    }

    @JvmStatic
    @Suppress("Unused")
    fun e(message: String) {
        setTag()
        Timber.e(message)
    }

    @JvmStatic
    @Suppress("Unused")
    fun e(throwable: Throwable?, message: String, vararg args: Any?) {
        setTag()
        Timber.e(throwable, message, args)
    }

    @JvmStatic
    @Suppress("Unused")
    fun i(message: String, vararg args: Any?) {
        setTag()
        Timber.i(message, args)
    }

    @JvmStatic
    @Suppress("Unused")
    fun v(message: String, vararg args: Any?) {
        setTag()
        Timber.v(message, args)
    }

    @JvmStatic
    @Suppress("Unused")
    fun w(message: String, vararg args: Any?) {
        setTag()
        Timber.w(message, args)
    }

    private fun setTag() {
        val trace = Thread.currentThread().stackTrace
        if(trace.size <= 4) return
        Timber.tag(getSimpleClassName(trace[4].className))
    }

    private fun getSimpleClassName(name: String): String {
        val lastIndex = name.lastIndexOf(".")
        return name.substring(lastIndex + 1)
            .split('$')
            .first()
    }
}