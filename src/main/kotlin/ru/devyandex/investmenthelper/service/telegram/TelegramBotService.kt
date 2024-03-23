package ru.devyandex.investmenthelper.service.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import ru.devyandex.investmenthelper.constants.Constants.ENTER_TOKEN_MESSAGE
import ru.devyandex.investmenthelper.constants.Constants.HELP_MESSAGE
import ru.devyandex.investmenthelper.constants.Constants.WELCOME_MESSAGE
import ru.devyandex.investmenthelper.model.TelegramEvents.*
import ru.devyandex.investmenthelper.model.validNotCommand

@Service
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
                        replyMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(listOf(SHOW_MENU.button), listOf(HELP.button)),
                            oneTimeKeyboard = true,
                            resizeKeyboard = true
                        )
                    )
                }
                callbackQuery(callbackAnswerText = START.command) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return@callbackQuery),
                        text = "$WELCOME_MESSAGE $ENTER_TOKEN_MESSAGE",
                        replyMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(listOf(SHOW_MENU.button), listOf(HELP.button)),
                            oneTimeKeyboard = true,
                            resizeKeyboard = true
                        )
                    )
                }
                command(HELP.code) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = HELP_MESSAGE,
                        replyMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(listOf(SHOW_MENU.button), listOf(START.button)),
                            oneTimeKeyboard = true,
                            resizeKeyboard = true
                        )
                    )
                }
                callbackQuery(callbackAnswerText = HELP.command) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return@callbackQuery),
                        text = HELP_MESSAGE,
                        replyMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(listOf(SHOW_MENU.button), listOf(START.button)),
                            oneTimeKeyboard = true,
                            resizeKeyboard = true
                        )
                    )
                }
                text {
                    if (text.validNotCommand()) {
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

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}