package org.ama.delivery.core.tests

import org.ama.delivery.core.domain.services.DispatchService
import org.ama.delivery.core.domain.services.IDispatchService
import org.koin.dsl.module

val testModuleCore = module {
    single<IDispatchService> { DispatchService() }
    // single<IDispatchService> { MyMockDispatchService() }
}