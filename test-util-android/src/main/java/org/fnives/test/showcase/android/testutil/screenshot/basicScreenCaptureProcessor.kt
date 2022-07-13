package org.fnives.test.showcase.android.testutil.screenshot

import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.screenshot.basicScreenCaptureProcessor
import java.io.File

fun basicScreenCaptureProcessor(subDir: String = "test-screenshots") =
    basicScreenCaptureProcessor(File(getTestPicturesDir(), subDir))

fun getTestPicturesDir() =
    InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
