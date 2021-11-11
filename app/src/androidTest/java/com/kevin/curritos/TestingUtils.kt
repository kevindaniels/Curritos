package com.kevin.curritos

import android.view.View
import androidx.annotation.CheckResult
import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.espresso.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Assert
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun getAssetFileContents(filePath: String): String {
    return getAssetFileInputStream(filePath)
        .bufferedReader()
        .use { it.readText() }
}

private fun getAssetFileInputStream(filePath: String): InputStream {
    return InstrumentationRegistry.getInstrumentation()
        .context
        .assets
        .open(filePath)
}

/**
 * Gets the value of a [LiveData] or waits for it to have one, with a timeout.
 *
 * Use this extension from host-side (JVM) tests. It's recommended to use it alongside
 * `InstantTaskExecutorRule` or a similar mechanism to execute tasks synchronously.
 */
// bump up the timeout in order to make sure that the tests not timeout on emulator.
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {},
    // KD Added this. If you want to get something DIFFERENT than the previous value returned
    // but the live data, pass this in.
    previous: T? = null,
    // KD Added this. If you are looking for a specific state/value to be returned by the live data,
    // you can set those conditions you're looking for. To see an example of this look at the class
    // SchedulerTest and look for calls to this that have "condition = { ... }"
    condition: ((data: T?) -> Boolean)? = null
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            val conditionMet = condition == null || condition(o)
            val isNotPrevious = previous == null || previous != o
            if(conditionMet && isNotPrevious) {
                data = o
                this@getOrAwaitValue.removeObserver(this)
                latch.countDown()
            }
        }
    }
    this.observeForever(observer)

    afterObserve.invoke()

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        this.removeObserver(observer)
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

/**
 * Observes a [LiveData] until the `block` is done executing.
 * This usually to test those live data which emit sequence of data.
 */
fun <T> LiveData<T>.observeForTesting(block: () -> Unit) {
    val observer = Observer<T> { }
    try {
        observeForever(observer)
        block()
    } finally {
        removeObserver(observer)
    }
}

inline fun <reified T> Gson.fromJson(json: String): T = fromJson(json, object: TypeToken<T>() {}.type)

internal object WaitForUIUpdate {
    fun waitForWithId(@IdRes resId: Int) {
        var element: ViewInteraction
        do {
            waitFor(200)
            //simple example using withText Matcher.
            element = Espresso.onView(ViewMatchers.withId(resId))
        } while (!MatcherExtension.exists(element))
    }

    fun waitForWithText(@IdRes stringId: Int) {
        var element: ViewInteraction
        do {
            waitFor(200)
            //simple example using withText Matcher.
            element = Espresso.onView(ViewMatchers.withText(stringId))
        } while (!MatcherExtension.exists(element))
    }

    fun waitFor(ms: Long) {
        val signal = CountDownLatch(1)
        try {
            signal.await(ms, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Assert.fail(e.message)
        }
    }
}

object MatcherExtension {
    @CheckResult
    fun exists(interaction: ViewInteraction): Boolean {
        return try {
            interaction.perform(object : ViewAction {

                override fun getDescription(): String {
                    return "check for existence"
                }

                override fun getConstraints(): Matcher<View> {
                    return Matchers.any(View::class.java)
                }

                override fun perform(uiController: UiController?, view: View?) {
                    // no op, if this is run, then the execution will continue after .perform(...)
                }
            })
            true
        } catch (ex: AmbiguousViewMatcherException) {
            // if there's any interaction later with the same matcher, that'll fail anyway
            true // we found more than one
        } catch (ex: NoMatchingViewException) {
            false
        } catch (ex: NoMatchingRootException) {
            // optional depending on what you think "exists" means
            false
        }
    }
}