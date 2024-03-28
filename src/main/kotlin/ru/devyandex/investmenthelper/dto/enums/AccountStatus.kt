package ru.devyandex.investmenthelper.dto.enums

enum class AccountStatus(val description: String) {
    ACCOUNT_STATUS_UNSPECIFIED("Статус счёта не определён"),
    ACCOUNT_STATUS_NEW("Новый, в процессе открытия"),
    ACCOUNT_STATUS_OPEN("Открытый и активный счёт"),
    ACCOUNT_STATUS_CLOSED("Закрытый счёт"),
}