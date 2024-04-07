package ru.devyandex.investmenthelper.util

import ru.devyandex.investmenthelper.constants.Constants.CURRENCY
import ru.devyandex.investmenthelper.dto.enums.AccountStatus
import ru.devyandex.investmenthelper.dto.enums.AccountType
import ru.devyandex.investmenthelper.dto.user.AccountDto
import ru.devyandex.investmenthelper.dto.user.AccountInfoDto
import java.math.BigDecimal

fun AccountDto.toMessage(accountInfoDto: AccountInfoDto?) =
    "${this.name}\n" +
    "Тип счета: ${AccountType.valueOf(this.type).description}\n" +
    "Статус: ${AccountStatus.valueOf(this.status).description}\n" +
    "Стоимость портфеля: ${accountInfoDto?.totalAmountPortfolio?.toNumberAmountWithoutCurrency() ?: BigDecimal(0)} ${CURRENCY.uppercase()}\n" +
    "Текущая доходность: ${accountInfoDto?.expectedYield}%\n"