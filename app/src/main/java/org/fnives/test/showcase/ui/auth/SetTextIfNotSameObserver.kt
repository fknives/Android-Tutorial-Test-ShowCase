package org.fnives.test.showcase.ui.auth

import android.widget.EditText
import androidx.lifecycle.Observer

class SetTextIfNotSameObserver(private val editText: EditText) : Observer<String> {
    override fun onChanged(t: String?) {
        val current = editText.text?.toString()
        if (current != t) {
            editText.setText(t)
        }
    }
}
