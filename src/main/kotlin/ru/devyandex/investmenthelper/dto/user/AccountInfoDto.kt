package ru.devyandex.investmenthelper.dto.user

import java.math.BigDecimal

data class AccountInfoDto(
    val totalAmountPortfolio: String,
    val expectedYield: BigDecimal
)