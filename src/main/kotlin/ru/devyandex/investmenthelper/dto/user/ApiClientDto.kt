package ru.devyandex.investmenthelper.dto.user

import ru.devyandex.investmenthelper.constants.Constants.TOKEN_PREFIX
import ru.devyandex.investmenthelper.dto.setting.CompanyStrategy
import ru.tinkoff.piapi.core.InvestApi
import java.util.concurrent.CopyOnWriteArrayList

data class ApiClientDto(
    val id: Long,
    //сгенерированный при помощи токена пользователя экземпляр АПИ
    val apiClient: InvestApi,
    //идентификатор счета
    var accountId: String? = null,
    //запущена ли работа с клиентом
    val isActive: Boolean = false,
    //список компаний, с которыми ведется работа
    val companyStrategies: CopyOnWriteArrayList<CompanyStrategy> = CopyOnWriteArrayList()
)

fun String.isToken() = this.startsWith(TOKEN_PREFIX)