package ru.devyandex.investmenthelper.service.core

import org.springframework.stereotype.Service
import ru.devyandex.investmenthelper.dto.integration.InvestApiResponse
import ru.devyandex.investmenthelper.dto.user.AccountDto
import ru.devyandex.investmenthelper.dto.user.AccountInfoDto
import ru.devyandex.investmenthelper.service.core.apiclient.InvestApiClientProvider
import ru.devyandex.investmenthelper.util.createErrorResponse
import ru.devyandex.investmenthelper.util.toAmountCurrencyString
import ru.devyandex.investmenthelper.util.toInvestApiResponse
import ru.tinkoff.piapi.core.utils.MapperUtils
import java.math.BigDecimal

@Service
//TODO Добавить логи
class UserService(
    private val apiClientProvider: InvestApiClientProvider
) {

    /**
     * Валидация токена и создание/обновление пользователя
     * @param id - Уникальный идентификатор пользователя
     * @param token - Токен для InvestApi
     *
     * @return Результат проверки токена. True если токен валиден, иначе false
     */
    fun authenticateToken(id: Long, token: String): Boolean =
        try {
            val client = apiClientProvider.upsertClient(id, token)

            client.apiClient.userService.infoSync

            true
        } catch (ex: Exception) {
            false
        }

    /**
     * Проверка на авторизацию пользователя по токену
     * @param id - Уникальный идентификатор пользователя
     *
     * @return True если пользователь уже авторизован, иначе false
     */
    fun clientExists(id: Long?) = id?.let {
        apiClientProvider.getClient(id) != null
    } ?: false

    /**
     * Удаление пользователя
     * @param id - Уникальный идентификатор пользователя
     */
    fun removeClient(id: Long) = apiClientProvider.removeClient(id)

    /**
     * Получение списка счетов в песочнице
     * @param id - Уникальный идентификатор пользователя
     *
     * @return Список счетов пользователя
     */
    fun getClientAccounts(id: Long): InvestApiResponse<List<AccountDto>?> {
        val client = apiClientProvider
            .getClient(id)
            ?: run {
                return createErrorResponse("Пользователь не найден")
            }

        return wrapMethod(errorMessage = "Не удалось получить счета") {
            client
                .apiClient
                .userService
                .accountsSync
                .map {
                    AccountDto(
                        id = it.id,
                        name = it.name,
                        type = it.type.name,
                        status = it.status.name
                    )
                }
        }
    }

    /**
     * Выбор счета для проведения операций
     * @param id - Уникальный идентификатор пользователя
     * @param accountId - Идентификатор счета
     *
     * @return Результат обновления выбранного счета. True если успешно, иначе false
     */
    fun selectAccount(id: Long, accountId: String): InvestApiResponse<Boolean?> {
        val client = apiClientProvider
            .getClient(id)
            ?: run {
                return createErrorResponse("Пользователь не найден")
            }

        /*
        TODO Стоит продумать алгоритм взаимодействия с хранилищем подключенных пользователей.
        Возможно, для некоторых ситуаций стоит добавить автоматическое создание экземпляра InvestApi
         */
        return apiClientProvider.updateAccount(id, accountId).toInvestApiResponse()
    }

    /**
     * Открытие нового счета в песочнице и его автоматическое пополнение
     * @param id - Уникальный идентификатор пользователя
     * @param name - Название нового счета
     *
     * @return идентификатор нового счета
     */
    fun openNewAccountAndPayIn(id: Long, name: String?): InvestApiResponse<String?> {
        val client = apiClientProvider
            .getClient(id)
            ?: run {
                return createErrorResponse("Пользователь не найден")
            }

        if (!client.apiClient.isSandboxMode) {
            return createErrorResponse("Открыть счет через апи можно только для песочницы")
        }

        return wrapMethod(errorMessage = "Не удалось открыть новый счет") {
            with(client.apiClient.sandboxService) {
                val accountId = this.openAccountSync(name)

                this.payInSync(accountId, MapperUtils.bigDecimalToMoneyValue(BigDecimal(100000), "rub"))

                accountId
            }
        }
    }

    /**
     * Закрытие счета в песочнице
     * @param id - Уникальный идентификатор пользователя
     * @param accountId - Идентификатор счета
     *
     * @return идентификатор закрытого счета
     */
    fun closeAccount(id: Long, accountId: String): InvestApiResponse<Unit?> {
        val client = apiClientProvider
            .getClient(id)
            ?: run {
                return createErrorResponse("Пользователь не найден")
            }

        if (!client.apiClient.isSandboxMode) {
            return createErrorResponse("Закрыть счет через апи можно только для песочницы")
        }

        return wrapMethod(errorMessage = "Не удалось закрыть счет") {
            client.apiClient.sandboxService.closeAccountSync(accountId)
        }
    }

    /**
     * Получение полной стоимости портфеля и доступного остатка для вывода средств
     * @param id - Уникальный идентификатор пользователя
     * @param accountId - Идентификатор счета
     *
     * @return Полная стоимость портфеля и доступный остаток для вывода средств
     */
    fun getAccountInfo(id: Long, accountId: String): InvestApiResponse<AccountInfoDto?> {
        val client = apiClientProvider
            .getClient(id)
            ?: run {
                return createErrorResponse("Пользователь не найден")
            }

        return wrapMethod(errorMessage = "Не удалось получить информацию по счету") {
            val portfolio = client
                .apiClient
                .operationsService
                .getPortfolioSync(accountId)

            val withdrawLimits = client
                .apiClient
                .operationsService
                .getWithdrawLimitsSync(accountId)

            AccountInfoDto(
                totalAmountPortfolio = portfolio.totalAmountPortfolio.toAmountCurrencyString(),
                withdrawLimits = withdrawLimits.money.map { it.toAmountCurrencyString() },
                withdrawLimitsBlocked = withdrawLimits.blocked.map { it.toAmountCurrencyString() },
                withdrawLimitsBlockedGuarantee = withdrawLimits.blockedGuarantee.map { it.toAmountCurrencyString() }
            )
        }
    }

    private fun <T> wrapMethod(errorMessage: String, block: () -> T?) =
        try {
            block().toInvestApiResponse()
        } catch (ex: Exception) {
            createErrorResponse(message = errorMessage, isError = true)
        }
}
