package org.fnives.test.showcase.android.testutil.intent

import android.content.Intent
import androidx.test.espresso.intent.Intents.intended
import org.hamcrest.Matcher
import org.hamcrest.StringDescription

fun notIntended(matcher: Matcher<Intent>) {
    try {
        intended(matcher)
    } catch (assertionError: AssertionError) {
        return
    }
    val description = StringDescription()
    matcher.describeMismatch(null, description)
    throw IllegalStateException("Navigate to intent found matching $description")
}
