package ru.devyandex.investmenthelper.service.core.strategy

import org.springframework.stereotype.Service
import ru.devyandex.investmenthelper.dto.strategy.StrategyEnum

@Service
class StrategyProvider(
    strategies: List<Strategy>
) {
    private val strategyMap = strategies.associateBy { it.getName() }

    fun getStrategy(name: StrategyEnum) = strategyMap[name]
}