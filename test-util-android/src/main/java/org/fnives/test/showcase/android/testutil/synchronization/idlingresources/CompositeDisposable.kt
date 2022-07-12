package org.fnives.test.showcase.android.testutil.synchronization.idlingresources

class CompositeDisposable(disposable: List<Disposable> = emptyList()) : Disposable {

    constructor(vararg disposables: Disposable) : this(disposables.toList())

    private val disposables = disposable.toMutableList()
    override val isDisposed: Boolean get() = disposables.all(Disposable::isDisposed)

    fun add(disposable: Disposable) {
        disposables.add(disposable)
    }

    override fun dispose() {
        disposables.forEach {
            it.dispose()
        }
    }
}
