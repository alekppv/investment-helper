package ru.devyandex.investmenthelper.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.devyandex.investmenthelper.service.core.apiclient.InvestApiClientProvider
import ru.devyandex.investmenthelper.service.core.apiclient.SandboxInvestApiClientProviderImpl

@Configuration
class InvestApiClientProviderConfig {
    @Bean
    @ConditionalOnProperty(name = ["helper.client-provider-type"], havingValue = "SANDBOX", matchIfMissing = true)
    fun sandboxInvestApiClientProvider(): InvestApiClientProvider = SandboxInvestApiClientProviderImpl()
}