package ru.devyandex.investmenthelper.dto.user

import ru.devyandex.investmenthelper.constants.Constants.TOKEN_PREFIX
import ru.tinkoff.piapi.core.InvestApi

data class ApiClientDto(
    //сгенерированный при помощи токена пользователя экземпляр АПИ
    val apiClient: InvestApi,
    //идентификатор счета
    var accountId: String? = null
)

fun String.isToken() = this.startsWith(TOKEN_PREFIX)