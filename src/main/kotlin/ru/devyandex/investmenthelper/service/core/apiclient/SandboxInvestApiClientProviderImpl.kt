package ru.devyandex.investmenthelper.service.core.apiclient

import ru.devyandex.investmenthelper.dto.user.ApiClientDto
import ru.tinkoff.piapi.core.InvestApi

class SandboxInvestApiClientProviderImpl : InvestApiClientProvider {

    private val clientStorage: MutableMap<Long, ApiClientDto> = mutableMapOf()

    override fun getClient(id: Long) = clientStorage[id]

    override fun upsertClient(id: Long, token: String): ApiClientDto {
        val clientApi = ApiClientDto(id, InvestApi.createSandbox(token))
        clientStorage[id] = clientApi

        return clientApi
    }

    override fun getActiveClients(): List<ApiClientDto> =
        clientStorage
            .values
            .filter { it.isActive }

    override fun removeClient(id: Long) {
        clientStorage.remove(id)
    }

    override fun updateAccount(id: Long, accountId: String): Boolean =
        clientStorage[id]
            ?.let {
                it.accountId = accountId

                true
            } ?: false
}