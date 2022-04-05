package org.fnives.test.showcase.ui.shared

@Suppress("DataClassContainsFunctions")
data class Event<T : Any>(private val data: T) {

    private var consumed: Boolean = false

    fun consume(): T? = data.takeUnless { consumed }?.also { consumed = true }

    fun peek() = data
}
