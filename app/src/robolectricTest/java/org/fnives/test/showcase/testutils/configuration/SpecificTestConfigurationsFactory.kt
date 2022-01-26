package org.fnives.test.showcase.testutils.configuration

object SpecificTestConfigurationsFactory : TestConfigurationsFactory {
    override fun createMainDispatcherTestRule(): MainDispatcherTestRule =
        TestCoroutineMainDispatcherTestRule()

    override fun createLoginRobotConfiguration(): LoginRobotConfiguration =
        RobolectricLoginRobotConfiguration

    override fun createSnackbarVerification(): SnackbarVerificationTestRule =
        RobolectricSnackbarVerificationTestRule

    override fun createSharedMigrationTestRuleFactory(): SharedMigrationTestRuleFactory =
        RobolectricMigrationTestHelperFactory
}
