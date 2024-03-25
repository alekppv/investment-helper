package ru.devyandex.investmenthelper.dto.user

import ru.tinkoff.piapi.core.InvestApi

data class ApiClientDto(
    //сгенерированный при помощи токена пользователя экземпляр АПИ
    val apiClient: InvestApi,
    //идентификатор счета
    var accountId: String? = null
)