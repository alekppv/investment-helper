package ru.devyandex.investmenthelper.service.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import ru.devyandex.investmenthelper.constants.Constants.ENTER_TOKEN_MESSAGE
import ru.devyandex.investmenthelper.constants.Constants.HELP_MESSAGE
import ru.devyandex.investmenthelper.constants.Constants.WELCOME_MESSAGE
import ru.devyandex.investmenthelper.model.TelegramEvents.*
import ru.devyandex.investmenthelper.model.isNotCommand

/**
 * Обработчик взаимодействия с telegram ботом
 * command {} - обработчик сообщений (начинающихся с "/") с командами
 * callbackQuery {} - обработчик нажатий кнопок
 * text {} - обработчик сообщений, не являющихся командами
 */
@Component
class TelegramBotService(
    @Value("\${telegram.token}")
    private val apiToken: String
) {

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        val bot = bot {
            token = apiToken
            dispatch {
                command(START.code) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = "$WELCOME_MESSAGE $ENTER_TOKEN_MESSAGE",
                        replyMarkup = getKeyboardReplyMarkup(SHOW_MENU.button, HELP.button)
                    )
                }
                callbackQuery(START.code) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return@callbackQuery),
                        text = "$WELCOME_MESSAGE $ENTER_TOKEN_MESSAGE",
                        replyMarkup = getKeyboardReplyMarkup(SHOW_MENU.button, HELP.button)
                    )
                }
                command(HELP.code) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = HELP_MESSAGE,
                        replyMarkup = getKeyboardReplyMarkup(SHOW_MENU.button, START.button)
                    )
                }
                callbackQuery(HELP.code) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return@callbackQuery),
                        text = HELP_MESSAGE,
                        replyMarkup = getKeyboardReplyMarkup(SHOW_MENU.button, START.button)
                    )
                }
                text {
                    if (text.isNotCommand()) {
                        logger.info { text }
                        TODO("Обработка токена для Тиньк API")
                    }
                }
                telegramError {
                    logger.error { error.getErrorMessage() }
                }
            }
        }

        bot.startPolling()
    }

    private fun getKeyboardReplyMarkup(vararg buttons: InlineKeyboardButton): InlineKeyboardMarkup {
        val keyboard: MutableList<List<InlineKeyboardButton>> = mutableListOf()


        buttons.map {
            keyboard.add(listOf(it))
        }

        return InlineKeyboardMarkup.create(keyboard)
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}