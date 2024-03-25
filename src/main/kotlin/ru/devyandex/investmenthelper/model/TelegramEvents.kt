package ru.devyandex.investmenthelper.model

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
    START("start", InlineKeyboardButton.CallbackData(text = "Запустить торгового робота", callbackData = "start")),
    HELP("help", InlineKeyboardButton.CallbackData(text = "Помощь по командам", callbackData = "help")),
    SHOW_MENU("menu", InlineKeyboardButton.CallbackData(text = "Основные команды", callbackData = "menu")),
    SHOW_TICKERS("tickers", InlineKeyboardButton.CallbackData(text = "Торговые инструменты", callbackData = "tickers")),
    SHOW_STRATEGIES("strategies", InlineKeyboardButton.CallbackData(text = "Торговые стратегии", callbackData = "strategies"))
}

fun String.isNotCommand(): Boolean {
    val commands = TelegramEvents.entries.map { "/${it.code}" } + TelegramEvents.entries.map { it.name }

    return !commands.contains(this)
}