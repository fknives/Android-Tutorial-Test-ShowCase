package org.fnives.test.showcase.android.testutil.screenshot

import android.graphics.Bitmap
import android.util.Log
import androidx.test.runner.screenshot.ScreenCapture
import androidx.test.runner.screenshot.ScreenCaptureProcessor
import androidx.test.runner.screenshot.Screenshot
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.IOException

class ScreenshotRule(
    private val prefix: String = "",
    private val takeBefore: Boolean = false,
    private val takeOnSuccess: Boolean = false,
    private val takeOnFailure: Boolean = true,
    private val timestampSuffix: Boolean = true,
    private val processor: ScreenCaptureProcessor = basicScreenCaptureProcessor(),
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        if (takeBefore) {
            takeScreenshot(baseName = description.beforeTestScreenshotName)
        }
    }

    override fun failed(e: Throwable?, description: Description) {
        super.failed(e, description)
        if (takeOnFailure) {
            takeScreenshot(baseName = description.failTestScreenshotName)
        }
    }

    override fun succeeded(description: Description) {
        super.succeeded(description)
        if (takeOnSuccess) {
            takeScreenshot(baseName = description.successTestScreenshotName)
        }
    }

    fun takeScreenshot(prefix: String = this.prefix, baseName: String, capture: ScreenCapture = Screenshot.capture()) {
        val fileName = if (timestampSuffix) {
            "$prefix-$baseName-${System.currentTimeMillis()}"
        } else {
            "$prefix-$baseName"
        }
        takeScreenshot(filename = fileName, capture = capture)
    }

    @Suppress("PrintStackTrace")
    private fun takeScreenshot(filename: String, capture: ScreenCapture) {
        capture.name = filename
        capture.format = Bitmap.CompressFormat.JPEG
        try {
            capture.process(setOf(processor))
            Log.d(TAG, "Saved image: $filename")
        } catch (e: IOException) {
            Log.d(TAG, "Couldn't save image: $e")
            e.printStackTrace()
        }
    }

    companion object {
        const val TAG = "Screenshot Rule"
        val Description.testScreenshotName get() = "${testClass.simpleName}-$methodName"
        val Description.beforeTestScreenshotName get() = "$testScreenshotName-BEFORE"
        val Description.successTestScreenshotName get() = "$testScreenshotName-SUCCESS"
        val Description.failTestScreenshotName get() = "$testScreenshotName-FAIL"
    }
}
