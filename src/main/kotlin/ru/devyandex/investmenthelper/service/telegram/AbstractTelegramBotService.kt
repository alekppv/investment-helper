package ru.devyandex.investmenthelper.service.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.types.TelegramBotResult
import ru.devyandex.investmenthelper.constants.Constants
import ru.devyandex.investmenthelper.service.core.UserService

/**
 * Обработчик взаимодействия с telegram ботом
 * command {} - обработчик сообщений (начинающихся с "/") с командами
 * callbackQuery {} - обработчик нажатий кнопок
 * text {} - обработчик сообщений, не являющихся командами
 */
abstract class AbstractTelegramBotService(
    protected val apiToken: String,
    protected val userService: UserService
) {

    abstract fun init()

    protected fun getKeyboardReplyMarkup(vararg buttons: InlineKeyboardButton): InlineKeyboardMarkup {
        val keyboard: List<List<InlineKeyboardButton>> = buttons.map {
            listOf(it)
        }

        return InlineKeyboardMarkup.create(keyboard)
    }

    protected fun validateClient(
        bot: Bot,
        clientId: Long,
    ): Boolean =
        if (!userService.clientExists(clientId)) {
            bot.sendMessage(
                chatId = ChatId.fromId(clientId),
                text = "${Constants.USER_NOT_FOUND} ${Constants.ENTER_TOKEN_MESSAGE}",
            )
            false
        } else {
            true
        }
}