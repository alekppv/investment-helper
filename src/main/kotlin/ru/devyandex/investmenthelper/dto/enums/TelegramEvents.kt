package ru.devyandex.investmenthelper.dto.enums

import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

/**
 * Описание событий для телеграм бота.
 * @param code - команда для бота, которая обрабатывается как сообщение, начинающееся с '/'
 * @param button - соответствующая кнопка для команды
 */
enum class TelegramEvents(
    val code: String,
    val button: InlineKeyboardButton
) {
    /** Основные операции */
    START("start", InlineKeyboardButton.CallbackData(text = "Основное меню", callbackData = "start")),
    HELP("help", InlineKeyboardButton.CallbackData(text = "Помощь по командам", callbackData = "help")),
    SHOW_USER_MENU("user_menu", InlineKeyboardButton.CallbackData(text = "Профиль", callbackData = "user_menu")),

    /** Операции со счетами */
    SHOW_ACCOUNTS("accounts", InlineKeyboardButton.CallbackData(text = "Все счета", callbackData = "accounts")),

    /** Операции для песочницы */
    OPEN_ACCOUNTS("open_accounts", InlineKeyboardButton.CallbackData(text = "Открыть счета", callbackData = "open_accounts")),
    CLOSE_ACCOUNTS("close_accounts", InlineKeyboardButton.CallbackData(text = "Закрыть счета", callbackData = "close_accounts"))
}

fun String.isNotCommand(): Boolean {
    val commands = TelegramEvents.entries.map { "/${it.code}" } + TelegramEvents.entries.map { it.name }

    return !commands.contains(this)
}