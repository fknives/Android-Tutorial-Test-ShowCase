package org.fnives.test.showcase.android.testutil.screenshot

import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.screenshot.ScreenCaptureProcessor
import androidx.test.runner.screenshot.basicScreenCaptureProcessor
import java.io.File

fun basicScreenCaptureProcessor(subDir: String = "test-screenshots"): ScreenCaptureProcessor {
    val directory = File(getTestPicturesDir(), subDir)
    Log.d(ScreenshotRule.TAG, "directory to save screenshots = ${directory.absolutePath}")
    return basicScreenCaptureProcessor(File(getTestPicturesDir(), subDir))
}

/**
 * BasicScreenCaptureProcessor seems to work differently on API versions,
 * based on where we have access to save and pull the images from.
 *
 * see example issue: https://github.com/android/android-test/issues/818
 */
@Suppress("DEPRECATION")
fun getTestPicturesDir(): File? =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        Log.d(ScreenshotRule.TAG, "context.internal folder")

        InstrumentationRegistry.getInstrumentation().targetContext.filesDir
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        val environmentFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val externalFolder = File(environmentFolder, packageName)
        Log.d(ScreenshotRule.TAG, "environment.external folder")

        externalFolder
    } else {
        Log.d(ScreenshotRule.TAG, "context.external folder")

        InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    }
