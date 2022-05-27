package org.fnives.test.showcase.android.testutil.synchronization.idlingresources

interface Disposable {
    val isDisposed: Boolean
    fun dispose()
}
