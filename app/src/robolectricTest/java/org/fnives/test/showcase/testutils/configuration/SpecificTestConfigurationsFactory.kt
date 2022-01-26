package org.fnives.test.showcase.testutils.configuration

object SpecificTestConfigurationsFactory : TestConfigurationsFactory {

    override fun createSnackbarVerification(): SnackbarVerificationHelper =
        RobolectricSnackbarVerificationHelper

    override fun createSharedMigrationTestRuleFactory(): SharedMigrationTestRuleFactory =
        RobolectricMigrationTestHelperFactory
}
