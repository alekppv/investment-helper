package ru.devyandex.investmenthelper.service.core.rule

import org.ta4j.core.Indicator
import org.ta4j.core.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.rules.AbstractRule
import kotlin.math.max

class StopLossOnIndicatorRule(
    private val indicator: Indicator<Num>
): AbstractRule() {

    override fun isSatisfied(index: Int, tradingRecord: TradingRecord?): Boolean {
        var satisfied = false
        // No trading history or no position opened, no loss
        if (tradingRecord != null) {
            val currentPosition = tradingRecord.currentPosition
            if (currentPosition.isOpened) {
                val entryPrice = currentPosition.entry.netPrice
                val currentIndicator: Num = indicator.getValue(max(index - 1, 0))
                satisfied = if (currentPosition.entry.isBuy) {
                    isBuyStopSatisfied(entryPrice, currentIndicator)
                } else {
                    isSellStopSatisfied(entryPrice, currentIndicator)
                }
            }
        }
        traceIsSatisfied(index, satisfied)
        return satisfied
    }

    private fun isBuyStopSatisfied(entryPrice: Num, currentIndicator: Num): Boolean {
        return currentIndicator.isLessThanOrEqual(entryPrice)
    }

    private fun isSellStopSatisfied(entryPrice: Num, currentPrice: Num): Boolean {
        return currentPrice.isGreaterThanOrEqual(entryPrice)
    }
}