package org.fnives.test.showcase.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import org.koin.androidx.viewmodel.ext.android.viewModel

inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModels(): Lazy<T> =
    viewModel()
