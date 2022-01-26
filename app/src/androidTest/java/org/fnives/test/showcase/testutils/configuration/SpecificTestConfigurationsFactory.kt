package org.fnives.test.showcase.testutils.configuration

object SpecificTestConfigurationsFactory : TestConfigurationsFactory {

    override fun createSnackbarVerification(): SnackbarVerificationHelper =
        AndroidTestSnackbarVerificationHelper

    override fun createSharedMigrationTestRuleFactory(): SharedMigrationTestRuleFactory =
        AndroidMigrationTestRuleFactory
}
