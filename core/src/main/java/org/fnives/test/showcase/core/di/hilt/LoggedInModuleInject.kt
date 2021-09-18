package org.fnives.test.showcase.core.di.hilt

import org.fnives.library.reloadable.module.annotation.ReloadableModule

@ReloadableModule
@Target(AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
annotation class LoggedInModuleInject
