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
import ru.devyandex.investmenthelper.constants.Constants.WELCOME_MESSAGE
import ru.devyandex.investmenthelper.dto.enums.AccountStatus
import ru.devyandex.investmenthelper.dto.enums.AccountType
import ru.devyandex.investmenthelper.dto.enums.TelegramEvents.*
import ru.devyandex.investmenthelper.dto.enums.isNotCommand
import ru.devyandex.investmenthelper.dto.user.isToken
import ru.devyandex.investmenthelper.service.core.UserService
import java.math.BigDecimal

@Service
class TelegramBotService(
    @Value("\${telegram.token}")
    apiToken: String,
    userService: UserService
) : AbstractTelegramBotService(apiToken, userService) {

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
                            replyMarkup = getKeyboardReplyMarkup(SHOW_ACCOUNTS.button, OPEN_ACCOUNTS.button)
                        )
                    }
                }
                callbackQuery(SHOW_ACCOUNTS.code) {
                    val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery

                    if (validateClient(bot, chatId)) {
                        val accounts = userService.getClientAccounts(chatId)
                        val accountsInfo = accounts.data?.associate { account ->
                            account.id to userService.getAccountInfo(chatId, account.id)
                        }

                        if (accountsInfo.isNullOrEmpty()) {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = Constants.ACCOUNTS_NOT_FOUND_MESSAGE,
                                replyMarkup = getKeyboardReplyMarkup(OPEN_ACCOUNTS.button)
                            )
                        } else {
                            val msg = accounts.data.joinToString(separator = "\n") {
                                "${it.name}\n" +
                                        "Тип счета: ${AccountType.valueOf(it.type).description}\n" +
                                        "Статус: ${AccountStatus.valueOf(it.status).description}\n" +
                                        "Стоимость портфеля: ${accountsInfo[it.id]?.data?.totalAmountPortfolio ?: BigDecimal(0)}"
                            }
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = msg
                            )
                        }
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
                                        SHOW_ACCOUNTS.button,
                                        //OPEN_ACCOUNTS.button,
                                        //CLOSE_ACCOUNTS.button
                                    )
                                )
                            } else {
                                userService.removeClient(userId)
                                bot.sendMessage(
                                    chatId = ChatId.fromId(userId),
                                    text = Constants.INVALID_TOKEN_MESSAGE,
                                )
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

                        val firstAccount = userService.openNewAccountAndPayIn(chatId, accountFirstName)

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

                        val secondAccount = userService.openNewAccountAndPayIn(chatId, accountSecondName)

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
                        userService.getClientAccounts(chatId).data
                            ?.forEach {
                                userService.closeAccount(chatId, it.id)
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