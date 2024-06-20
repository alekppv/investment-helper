package ru.devyandex.investmenthelper.service.core

import org.springframework.stereotype.Service
import ru.devyandex.investmenthelper.service.core.apiclient.InvestApiClientProvider
import ru.devyandex.investmenthelper.service.core.strategy.StrategyProvider

/**
 * Сервис управления расчетами. Отдает наружу метод запуска расчетов (для вызова по крону, например)
 * Получает настройки клиентов, запускает обработку по этим параметрам
 */
@Service
class Orchestrator(
    private val apiClientProvider: InvestApiClientProvider,
    private val strategyProvider: StrategyProvider
) {

    fun startProcessing() {
        apiClientProvider
            .getActiveClients()
            .forEach { client ->
                client.companyStrategies.forEach { company ->
                    strategyProvider
                        .getStrategy(company.strategy)
                        ?.startProcessing(client.id, company)
                }
            }
    }
}