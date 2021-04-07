package org.fnives.test.showcase.testutils.idling

interface Disposable {
    val isDisposed: Boolean
    fun dispose()
}
