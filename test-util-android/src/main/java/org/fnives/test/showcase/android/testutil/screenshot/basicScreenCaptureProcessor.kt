package org.fnives.test.showcase.android.testutil.screenshot

import android.os.Build
import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.screenshot.basicScreenCaptureProcessor
import java.io.File

fun basicScreenCaptureProcessor(subDir: String = "test-screenshots") =
    basicScreenCaptureProcessor(File(getTestPicturesDir(), subDir))

@Suppress("DEPRECATION")
fun getTestPicturesDir() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    } else {
        val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), packageName)
    }
