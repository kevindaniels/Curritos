package com.kevin.curritos

import android.Manifest
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.kevin.curritos.base.Logger
import com.kevin.curritos.module.AppModule.APP_DIRECTORY_KEY
import com.kevin.curritos.module.NetworkModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@UninstallModules(NetworkModule::class)
@HiltAndroidTest
class ListFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    var mRuntimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET
    )

    @Inject
    @Named(APP_DIRECTORY_KEY)
    lateinit var appFolder: String

    @Before
    fun setUp() {
        hiltRule.inject()
        TestNetworkModule.server = MockWebServer()
        TestNetworkModule.server.start(TestNetworkModule.port)
    }

    @After
    fun tearDown() {
        TestNetworkModule.server.shutdown()
    }

    @Test
    fun test_empty_search() {
        TestNetworkModule.server.enqueue(MockResponse().setBody(getAssetFileContents("response_empty.json")))
        WaitForUIUpdate.waitForWithText(R.string.list_search_no_results)
        takeScreenShot("list_fragment_empty")
    }

    @Test
    fun test_results_search() {
        TestNetworkModule.server.enqueue(MockResponse().setBody(getAssetFileContents("response.json")))
        WaitForUIUpdate.waitForWithId(R.id.business_image)
        takeScreenShot("list_fragment_results")
    }

    @Test
    fun test_location_error_search() {
        TestNetworkModule.server.enqueue(MockResponse().setBody(getAssetFileContents("response_location_error.json")))
        WaitForUIUpdate.waitForWithText(R.string.list_search_location_error)
        takeScreenShot("list_fragment_location_error")
    }

    @Test
    fun test_generic_error_search() {
        TestNetworkModule.server.enqueue(MockResponse().setBody(getAssetFileContents("response_error.json")))
        WaitForUIUpdate.waitForWithText(R.string.list_search_generic_error)
        takeScreenShot("list_fragment_generic_error")
    }

    // Can move all these two functions below into a base ScreenshotTest class that can be used for
    // any test we want to take screenshots of
    private fun takeScreenShot(imageName: String) {
        Thread.sleep(100)
        val screenshotFolder = appFolder + "test_screenshots/"
        createFolderIfNotExist(screenshotFolder)
        val file = File("$screenshotFolder$imageName.png")
        if (!file.exists()) {
            file.createNewFile()
        }
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .takeScreenshot(file, 0.5f, 10)
    }

    private fun createFolderIfNotExist(folderPath: String) {
        val folder = File(folderPath)
        if (!folder.exists()) {
            Logger.i("Create folder $folderPath")
            folder.mkdirs()
        }
    }
}