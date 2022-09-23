package org.fnives.test.showcase.examplecase.navcontroller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// to see the actual screen, not just in test use:
// adb shell am start -n org.fnives.test.showcase/org.fnives.test.showcase.examplecase.navcontroller.NavControllerActivity
// after installing the apk
class NavControllerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav_controller)
    }
}
