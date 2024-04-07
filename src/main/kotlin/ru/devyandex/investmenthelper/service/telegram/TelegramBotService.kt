package ru.devyandex.investmenthelper.service.telegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import ru.devyandex.investmenthelper.constants.Constants
import ru.devyandex.investmenthelper.constants.Constants.AUTH_SUCCESS_MESSAGE
import ru.devyandex.investmenthelper.constants.Constants.HELP_MESSAGE
import ru.devyandex.investmenthelper.constants.Constants.OPERATION_ERROR_MESSAGE
import ru.devyandex.investmenthelper.constants.Constants.WELCOME_MESSAGE
import ru.devyandex.investmenthelper.dto.enums.TelegramEvents.*
import ru.devyandex.investmenthelper.dto.enums.isNotCommand
import ru.devyandex.investmenthelper.dto.user.isToken
import ru.devyandex.investmenthelper.service.core.AccountService
import ru.devyandex.investmenthelper.service.core.UserService
import ru.devyandex.investmenthelper.util.toMessage

@Service
class TelegramBotService(
    @Value("\${telegram.token}")
    apiToken: String,
    userService: UserService,
    accountService: AccountService
) : AbstractTelegramBotService(apiToken, userService, accountService) {

    @EventListener(ApplicationReadyEvent::class)
    override fun init() {
        val bot = bot {
            token = apiToken
            dispatch {
                command(START.code) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = WELCOME_MESSAGE,
                        replyMarkup = getKeyboardReplyMarkup(SHOW_USER_MENU.button, HELP.button)
                    )
                }
                callbackQuery(START.code) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return@callbackQuery),
                        text = WELCOME_MESSAGE,
                        replyMarkup = getKeyboardReplyMarkup(SHOW_USER_MENU.button, HELP.button)
                    )
                }
                command(HELP.code) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = HELP_MESSAGE,
                        replyMarkup = getKeyboardReplyMarkup(START.button)
                    )
                }
                callbackQuery(HELP.code) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(callbackQuery.message?.chat?.id ?: return@callbackQuery),
                        text = HELP_MESSAGE,
                        replyMarkup = getKeyboardReplyMarkup(START.button)
                    )
                }
                callbackQuery(SHOW_USER_MENU.code) {
                    val clientId = callbackQuery.message?.chat?.id ?: return@callbackQuery

                    if (validateClient(bot, clientId)) {
                        bot.sendMessage(
                            chatId = ChatId.fromId(clientId),
                            text = AUTH_SUCCESS_MESSAGE,
                            replyMarkup = getKeyboardReplyMarkup(SHOW_ACCOUNTS.button)
                        )
                    } else {
                        logger.error { "Пользователь ${callbackQuery.from.username} не авторизован!" }
                    }
                }
                callbackQuery(SHOW_ACCOUNTS.code) {
                    val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery

                    if (validateClient(bot, chatId)) {
                        val accounts = accountService.getClientAccounts(chatId)
                        val accountsInfo = accounts.data?.let {
                            it.associate { account ->
                                account.id to accountService.getAccountInfo(chatId, account.id)
                            }
                        } ?: run {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = accounts.message ?: OPERATION_ERROR_MESSAGE
                            )
                            logger.error { accounts.message }
                            return@callbackQuery
                        }

                        if (accountsInfo.isEmpty()) {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = Constants.ACCOUNTS_NOT_FOUND_MESSAGE,
                                replyMarkup = getKeyboardReplyMarkup(OPEN_ACCOUNTS.button)
                            )
                        } else {
                            accounts.data.forEach { account ->
                                bot.sendMessage(
                                    chatId = ChatId.fromId(chatId),
                                    text = account.toMessage(accountsInfo[account.id]?.data)
                                )
                            }
                        }
                    } else {
                        logger.error { "Пользователь ${callbackQuery.from.username} не авторизован!" }
                    }
                }
                text {
                    val userId = message.chat.id
                    when {
                        text.isNotCommand() && text.isToken() -> {
                            val token = text
                                .removePrefix(Constants.TOKEN_PREFIX)
                                .trimIndent()
                            val authResult = userService.authenticateToken(userId, token)

                            if (authResult) {
                                bot.sendMessage(
                                    chatId = ChatId.fromId(userId),
                                    text = AUTH_SUCCESS_MESSAGE,
                                    replyMarkup = getKeyboardReplyMarkup(
                                        SHOW_ACCOUNTS.button
                                    )
                                )
                            } else {
                                userService.removeClient(userId)
                                bot.sendMessage(
                                    chatId = ChatId.fromId(userId),
                                    text = Constants.INVALID_TOKEN_MESSAGE,
                                )
                                logger.error { "Пользователь ${message.chat.username} ввел неправильный токен для Tinkoff API" }
                            }
                        }
                    }
                }

                /**
                 * ДЛЯ ПЕСОЧНИЦЫ
                 */
                callbackQuery(OPEN_ACCOUNTS.code) {
                    val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery

                    if (validateClient(bot, chatId)) {
                        val accountFirstName = "contest2024:alekppv/investment-helper:1"
                        val accountSecondName = "contest2024:alekppv/investment-helper:2"

                        val firstAccount = accountService.openNewAccountAndPayIn(chatId, accountFirstName)

                        when {
                            firstAccount.isSuccessful -> bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "${Constants.ACCOUNT_OPEN_SUCCESS_MESSAGE}$accountFirstName",
                                replyMarkup = getKeyboardReplyMarkup(SHOW_ACCOUNTS.button)
                            )
                            firstAccount.isError -> bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = Constants.ACCOUNT_OPEN_ERROR_MESSAGE,
                                replyMarkup = getKeyboardReplyMarkup(OPEN_ACCOUNTS.button)
                            )
                            else -> { }
                        }

                        val secondAccount = accountService.openNewAccountAndPayIn(chatId, accountSecondName)

                        when {
                            secondAccount.isSuccessful -> bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "${Constants.ACCOUNT_OPEN_SUCCESS_MESSAGE}$accountSecondName",
                                replyMarkup = getKeyboardReplyMarkup(SHOW_ACCOUNTS.button)
                            )
                            secondAccount.isError -> bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = Constants.ACCOUNT_OPEN_ERROR_MESSAGE,
                                replyMarkup = getKeyboardReplyMarkup(OPEN_ACCOUNTS.button)
                            )
                            else -> { }
                        }
                    }
                }
                callbackQuery(CLOSE_ACCOUNTS.code) {
                    val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery

                    if (validateClient(bot, chatId)) {
                        accountService.getClientAccounts(chatId).data
                            ?.forEach {
                                accountService.closeAccount(chatId, it.id)
                            }
                    }
                }
            }
        }

        bot.startPolling()
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}