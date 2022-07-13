package org.fnives.test.showcase.android.testutil.screenshot

import android.graphics.Bitmap
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

    fun takeScreenshot(prefix: String = this.prefix, baseName: String) {
        val fileName = if (timestampSuffix) {
            "$prefix-$baseName-${System.currentTimeMillis()}"
        } else {
            "$prefix-$baseName"
        }
        takeScreenshot(filename = fileName)
    }

    @Suppress("PrintStackTrace")
    private fun takeScreenshot(filename: String) {
        val capture: ScreenCapture = Screenshot.capture()
        capture.name = filename
        capture.format = Bitmap.CompressFormat.JPEG
        try {
            capture.process(setOf(processor))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        val Description.testScreenshotName get() = "${testClass.simpleName}-$methodName"
        val Description.beforeTestScreenshotName get() = "$testScreenshotName-BEFORE"
        val Description.successTestScreenshotName get() = "$testScreenshotName-SUCCESS"
        val Description.failTestScreenshotName get() = "$testScreenshotName-FAIL"
    }
}
