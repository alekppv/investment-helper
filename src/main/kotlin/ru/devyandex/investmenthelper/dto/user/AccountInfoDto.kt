package ru.devyandex.investmenthelper.dto.user

data class AccountInfoDto(
    val totalAmountPortfolio: String,
    val withdrawLimits: List<String>,
    val withdrawLimitsBlocked: List<String>,
    val withdrawLimitsBlockedGuarantee: List<String>
)