package ru.devyandex.investmenthelper.service.core.apiclient

import ru.devyandex.investmenthelper.dto.user.ApiClientDto

interface InvestApiClientProvider {
    fun getClient(id: Long): ApiClientDto?

    fun getActiveClients(): List<ApiClientDto>

    fun upsertClient(id: Long, token: String): ApiClientDto

    fun removeClient(id: Long)

    fun updateAccount(id: Long, accountId: String): Boolean
}