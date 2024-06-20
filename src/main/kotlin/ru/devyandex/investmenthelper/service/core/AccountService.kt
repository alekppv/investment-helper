package ru.devyandex.investmenthelper.service.core

import org.springframework.stereotype.Service
import ru.devyandex.investmenthelper.dto.integration.InvestApiResponse
import ru.devyandex.investmenthelper.dto.user.AccountDto
import ru.devyandex.investmenthelper.dto.user.AccountInfoDto
import ru.devyandex.investmenthelper.service.core.apiclient.InvestApiClientProvider
import ru.devyandex.investmenthelper.util.createErrorResponse
import ru.devyandex.investmenthelper.util.toAmountCurrencyString
import ru.devyandex.investmenthelper.util.wrapMethod
import ru.tinkoff.piapi.core.utils.MapperUtils
import java.math.BigDecimal

@Service
class AccountService(
    private val apiClientProvider: InvestApiClientProvider
) {
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

            AccountInfoDto(
                totalAmountPortfolio = portfolio.totalAmountPortfolio.toAmountCurrencyString(),
                expectedYield = portfolio.expectedYield.setScale(2)
            )
        }
    }
}