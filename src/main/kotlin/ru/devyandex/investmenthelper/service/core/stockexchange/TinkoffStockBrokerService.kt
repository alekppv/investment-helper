package ru.devyandex.investmenthelper.service.core.stockexchange

import org.springframework.stereotype.Service
import ru.devyandex.investmenthelper.dto.instruments.Share
import ru.devyandex.investmenthelper.service.core.UserService
import ru.tinkoff.piapi.contract.v1.InstrumentStatus
import ru.tinkoff.piapi.contract.v1.RealExchange
import ru.tinkoff.piapi.contract.v1.ShareType

@Service
class TinkoffStockBrokerService(
   private val userService: UserService
) {

    fun getShares(clientId: Long): List<Share> {
        val client = userService.getClientById(clientId)

        return client?.let {
            client.apiClient
                .instrumentsService
                .getSharesSync(AVAILABLE_TINKOFF_SHARES)
                .filter {
                    SHARE_TYPES.contains(it.shareType) &&
                            it.apiTradeAvailableFlag &&
                            !it.forQualInvestorFlag &&
                            it.buyAvailableFlag &&
                            it.sellAvailableFlag &&
                            it.realExchange == RealExchange.REAL_EXCHANGE_MOEX

                }
                .map {
                    Share(
                        ticker = it.ticker,
                        lot = it.lot,
                        name = it.name,
                        shortEnabledFlag = it.shortEnabledFlag
                    )
                }
        } ?: emptyList()
    }

    companion object {
        private val AVAILABLE_TINKOFF_SHARES = InstrumentStatus.forNumber(1)
        private val SHARE_TYPES = listOf(ShareType.forNumber(1), ShareType.forNumber(2))
    }
}