package ru.devyandex.investmenthelper.dto.strategy

import org.ta4j.core.Indicator
import org.ta4j.core.num.Num

/**
 * Обертка над интерфейсом Indicator.
 * Нужна для составления отчетов/графиков по стратегиям
 *
 * @property name - Имя индикатора для дальнейшего отображения
 */
data class NumIndicatorWithName(
    val indicator: Indicator<Num>,
    val name: String
)