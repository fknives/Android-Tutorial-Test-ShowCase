package org.fnives.test.showcase.android.testutil.screenshot

import android.os.Environment
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.screenshot.ScreenCaptureProcessor
import androidx.test.runner.screenshot.basicScreenCaptureProcessor
import java.io.File

fun basicScreenCaptureProcessor(subDir: String = "test-screenshots"): ScreenCaptureProcessor {
    val directory = File(getTestPicturesDir(), subDir)
    Log.d(ScreenshotRule.TAG, "directory to save screenshots = $directory")
    return basicScreenCaptureProcessor(File(getTestPicturesDir(), subDir))
}

/**
 * BasicScreenCaptureProcessor seems to work differently on API versions,
 * based on where we have access to save and pull the images from.
 *
 * see example issue: https://github.com/android/android-test/issues/818
 */
@Suppress("DEPRECATION")
fun getTestPicturesDir(): File? {
    val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
    val environmentFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val externalFolder = File(environmentFolder, packageName)
    if (externalFolder.canWrite()) {
        Log.d(ScreenshotRule.TAG, "external folder")
        return externalFolder
    }

    val internalFolder = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    if (internalFolder?.canWrite() == true) {
        Log.d(ScreenshotRule.TAG, "internal folder")
        return internalFolder
    }
    Log.d(ScreenshotRule.TAG, "cant find directory the screenshots could be saved into")

    return null
}
