package ru.devyandex.investmenthelper.dto.enums

enum class AccountType(val description: String) {
    ACCOUNT_TYPE_UNSPECIFIED("Тип аккаунта не определён"),
    ACCOUNT_TYPE_TINKOFF("Брокерский счёт"),
    ACCOUNT_TYPE_TINKOFF_IIS("ИИС"),
    ACCOUNT_TYPE_INVEST_BOX("Инвесткопилка");
}