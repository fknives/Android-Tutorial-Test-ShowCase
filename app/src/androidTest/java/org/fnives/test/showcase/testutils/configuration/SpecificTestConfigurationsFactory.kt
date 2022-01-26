package org.fnives.test.showcase.testutils.configuration

object SpecificTestConfigurationsFactory : TestConfigurationsFactory {
    override fun createMainDispatcherTestRule(): MainDispatcherTestRule =
        AndroidTestMainDispatcherTestRule()

    override fun createLoginRobotConfiguration(): LoginRobotConfiguration =
        AndroidTestLoginRobotConfiguration

    override fun createSnackbarVerification(): SnackbarVerificationHelper =
        AndroidTestSnackbarVerificationHelper

    override fun createSharedMigrationTestRuleFactory(): SharedMigrationTestRuleFactory =
        AndroidMigrationTestRuleFactory
}
