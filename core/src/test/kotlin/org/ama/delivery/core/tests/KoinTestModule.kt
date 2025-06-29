package org.ama.delivery.core.tests

import org.ama.delivery.core.domain.services.DispatchService
import org.ama.delivery.core.domain.services.IDispatchService
import org.ama.delivery.core.domain.services.MyMockDispatchService
import org.koin.dsl.module

val testAppModule = module {
    single<IDispatchService> { DispatchService() }
    // single<IDispatchService> { MyMockDispatchService() }
}