package org.fnives.test.showcase.testutils.configuration

/**
 * Defines the platform specific configurations for Robolectric and AndroidTest.
 *
 * Each should have an object [SpecificTestConfigurationsFactory] implementing this interface so the SharedTests are
 * configured properly.
 */
interface TestConfigurationsFactory {

    fun createMainDispatcherTestRule(): MainDispatcherTestRule

    fun createServerTypeConfiguration(): ServerTypeConfiguration

    fun createLoginRobotConfiguration(): LoginRobotConfiguration

    fun createSnackbarVerification(): SnackbarVerificationTestRule
}
