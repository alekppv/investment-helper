package ru.devyandex.investmenthelper.dto.strategy

import org.ta4j.core.Strategy
import ru.devyandex.investmenthelper.dto.enums.SignalType

data class StrategyWrapper(
    val signalType: SignalType,
    val strategy: Strategy
)