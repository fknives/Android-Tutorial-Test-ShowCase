package org.fnives.test.showcase.network.mockserver.scenario.general

@Suppress("UnnecessaryAbstractClass")
abstract class GenericScenario<T : GenericScenario<T>> internal constructor() {

    internal var previousScenario: T? = null
        private set

    @Suppress("UNCHECKED_CAST")
    fun then(scenario: T): T {
        scenario.previousScenario = this as T
        return scenario
    }
}
