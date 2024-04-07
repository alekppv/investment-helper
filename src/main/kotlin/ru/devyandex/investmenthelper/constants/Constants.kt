package ru.devyandex.investmenthelper.constants

object Constants {
    const val WELCOME_MESSAGE = "Вас приветствует торговый робот. Для взаимодействия с роботом используйте меню."
    const val USER_NOT_FOUND = "Пользователь не найден."
    const val AUTH_SUCCESS_MESSAGE = "Успешная авторизация."
    const val ENTER_TOKEN_MESSAGE = "Для авторизации пользователя введите токен для вашего аккаунта в Тинькофф Инвестициях. " +
            "Его можно сгенерировать в личном кабинете Тинькофф Инвестиций."
    const val INVALID_TOKEN_MESSAGE = "Ваш токен неверен, попробуйте снова."
    const val ACCOUNTS_NOT_FOUND_MESSAGE = "Не найдено открытых счетов. Откройте счет чтобы начать торговать."
    const val ACCOUNT_OPEN_SUCCESS_MESSAGE = "Счет успешно открыт: "
    const val ACCOUNT_OPEN_ERROR_MESSAGE = "Ошибка при открытии счета, попробуйте снова."
    const val HELP_MESSAGE = "Для запуска торгового робота необходимо предоставить токен для взаимодействия с API " +
            "Тинькофф Инвестиций после команды \"Запустить торгового робота\". После этого необходимо выбрать торговую " +
            "стратегию из предложенных."
    const val OPERATION_ERROR_MESSAGE = "Ошибка при выполнении операции"

    const val TOKEN_PREFIX = "token:"
    const val CURRENCY = "rub"
}