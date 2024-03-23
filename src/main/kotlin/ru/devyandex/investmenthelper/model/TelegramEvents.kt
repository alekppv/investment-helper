package ru.devyandex.investmenthelper.model

import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton

enum class TelegramEvents(
    val code: String,
    val command: String,
    val button: KeyboardButton
) {
    START("start", "/Запустить торгового робота", KeyboardButton("Запустить торгового робота")),
    HELP("help", "/Помощь по командам", KeyboardButton("Помощь по командам")),
    SHOW_MENU("menu", "/Основные команды", KeyboardButton("Основные команды")),
    SHOW_TICKERS("tickers", "/Торговые инструменты", KeyboardButton("tickers")),
    SHOW_STRATEGIES("strategies", "/Торговые стратегии", KeyboardButton("strategies"))
}

fun String.validNotCommand(): Boolean {
    val commands = TelegramEvents.entries.map { it.code } + TelegramEvents.entries.map { it.command }

    return commands.contains(this)
}