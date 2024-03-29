package ru.devyandex.investmenthelper.service.core.strategy

import ru.devyandex.investmenthelper.dto.setting.CompanyStrategy
import ru.devyandex.investmenthelper.dto.strategy.StrategyEnum

interface Strategy {
    fun getName(): StrategyEnum

    fun startProcessing(id: Long, companyStrategy: CompanyStrategy)
}
