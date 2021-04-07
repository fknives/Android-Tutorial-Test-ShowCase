package org.fnives.test.showcase.ui.shared

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import coil.load
import coil.transform.RoundedCornersTransformation
import org.fnives.test.showcase.R
import org.fnives.test.showcase.model.content.ImageUrl

fun View.layoutInflater(): LayoutInflater = LayoutInflater.from(context)

fun ImageView.loadRoundedImage(imageUrl: ImageUrl) {
    load(imageUrl.url) {
        transformations(RoundedCornersTransformation(resources.getDimension(R.dimen.rounded_corner)))
    }
}

fun View.getThemePrimaryColor(): Int {
    val value = TypedValue()
    context.theme.resolveAttribute(R.attr.colorPrimary, value, true)
    return value.data
}
