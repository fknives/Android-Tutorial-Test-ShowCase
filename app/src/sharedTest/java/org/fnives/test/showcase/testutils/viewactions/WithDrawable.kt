package org.fnives.test.showcase.testutils.viewactions

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class WithDrawable(
    @DrawableRes
    private val id: Int,
    @ColorRes
    private val tint: Int? = null,
    private val tintMode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN
) : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
        description.appendText("ImageView with drawable same as drawable with id $id")
        tint?.let { description.appendText(", tint color id: $tint, mode: $tintMode") }
    }

    override fun matchesSafely(view: View): Boolean {
        val context = view.context
        val tintColor = tint?.let { ContextCompat.getColor(view.context, it) }
        val expectedBitmap = context.getDrawable(id)?.apply {
            if (tintColor != null) {
                setTintList(ColorStateList.valueOf(tintColor))
                setTintMode(tintMode)
            }
        }
        return view is ImageView && view.drawable.toBitmap().sameAs(expectedBitmap?.toBitmap())
    }
}
