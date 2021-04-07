package org.fnives.test.showcase.testutils.shadow

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.ContentViewCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadow.api.Shadow.extract
import java.lang.reflect.Modifier

@Implements(Snackbar::class)
class ShadowSnackbar {
    @RealObject
    var snackbar: Snackbar? = null
    var text: String? = null

    companion object {
        val shadowSnackbars = mutableListOf<ShadowSnackbar>()

        @Implementation
        @JvmStatic
        fun make(view: View, text: CharSequence, duration: Int): Snackbar? {
            var snackbar: Snackbar? = null
            try {
                val constructor = Snackbar::class.java.getDeclaredConstructor(
                    Context::class.java,
                    ViewGroup::class.java,
                    View::class.java,
                    ContentViewCallback::class.java
                ) ?: throw IllegalArgumentException("Seems like the constructor was not found!")
                if (Modifier.isPrivate(constructor.modifiers)) {
                    constructor.isAccessible = true
                }
                val parent = findSuitableParent(view)
                val content = LayoutInflater.from(parent.context)
                    .inflate(
                        com.google.android.material.R.layout.design_layout_snackbar_include,
                        parent,
                        false
                    ) as SnackbarContentLayout
                snackbar = constructor.newInstance(view.context, parent, content, content)
                snackbar.setText(text)
                snackbar.duration = duration
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
            shadowOf(snackbar).text = text.toString()
            shadowSnackbars.add(shadowOf(snackbar))
            return snackbar
        }

        private fun findSuitableParent(view: View): ViewGroup =
            when (view) {
                is CoordinatorLayout -> view
                is FrameLayout -> {
                    when {
                        view.id == R.id.content -> view
                        (view.parent as? View) == null -> view
                        else -> findSuitableParent(view.parent as View)
                    }
                }
                else -> {
                    when {
                        (view.parent as? View) == null && view is ViewGroup -> view
                        (view.parent as? View) == null -> FrameLayout(view.context)
                        else -> findSuitableParent(view.parent as View)
                    }
                }
            }

        @Implementation
        @JvmStatic
        fun make(view: View, @StringRes resId: Int, duration: Int): Snackbar? =
            make(view, view.resources.getText(resId), duration)

        fun shadowOf(bar: Snackbar?): ShadowSnackbar =
            extract(bar)

        fun reset() {
            shadowSnackbars.clear()
        }

        fun shownSnackbarCount(): Int = shadowSnackbars.size

        val textOfLatestSnackbar: String?
            get() = shadowSnackbars.lastOrNull()?.text
        val latestSnackbar: Snackbar?
            get() = shadowSnackbars.lastOrNull()?.snackbar
    }
}
