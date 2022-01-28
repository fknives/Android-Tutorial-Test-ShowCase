package org.fnives.test.showcase.testutils.configuration

object SpecificTestConfigurationsFactory : TestConfigurationsFactory {

    override fun createSharedMigrationTestRuleFactory(): SharedMigrationTestRuleFactory =
        AndroidMigrationTestRuleFactory
}
