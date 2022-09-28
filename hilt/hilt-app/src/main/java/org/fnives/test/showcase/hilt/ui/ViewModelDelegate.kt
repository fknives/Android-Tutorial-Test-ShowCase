package org.fnives.test.showcase.hilt.ui

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.activity.viewModels as androidxViewModel

inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModels(): Lazy<T> =
    when (this) {
        is ComponentActivity -> androidxViewModel()
        else -> throw IllegalStateException("Only supports activity viewModel for now")
    }
