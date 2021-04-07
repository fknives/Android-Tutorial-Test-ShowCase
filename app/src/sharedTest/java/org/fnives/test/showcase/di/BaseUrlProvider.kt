package org.fnives.test.showcase.di

import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.testutils.configuration.SpecificTestConfigurationsFactory

object BaseUrlProvider {

    fun get() = BaseUrl(SpecificTestConfigurationsFactory.createServerTypeConfiguration().url)
}
