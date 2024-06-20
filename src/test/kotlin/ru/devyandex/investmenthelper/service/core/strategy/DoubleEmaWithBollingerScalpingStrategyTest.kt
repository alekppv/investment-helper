package ru.devyandex.investmenthelper.service.core.strategy

import org.jfree.chart.ChartUtils
import org.junit.jupiter.api.Test
import ru.devyandex.investmenthelper.dto.enums.StrategyEnum
import ru.devyandex.investmenthelper.dto.setting.CompanyStrategy
import ru.devyandex.investmenthelper.service.core.marketdata.MarketDataFileMockService
import java.io.File
import java.io.FileOutputStream


class DoubleEmaWithBollingerScalpingStrategyTest {

    /**
     * Тест предназначен для выгрузки заранее подготовленных данных и отрисовки по ним графиков индикаторов.
     * Сохраняет в outputFileName график индикаторов
     */
    @Test
    fun simpleScalpingStrategyBackTest() {
        //Путь папки с файлами данных
        val baseFolder = "C:\\progs\\local"
        val folder = "$baseFolder\\data"

        var sum = 0.0

        File(folder)
            .listFiles()
            ?.forEach {
                //Путь выходного файла
                val outputFileName = "$baseFolder\\graphics\\chart_${it.name}.png"
                //Количество свечей с конца, по которым будет создан график
                val reportSize = 100

                val marketData = MarketDataFileMockService(it.absolutePath)

                val doubleEmaWithBollingerScalpingStrategy = DoubleEmaWithBollingerAndATRStrategy(marketData)
                doubleEmaWithBollingerScalpingStrategy.startProcessing(1, CompanyStrategy("", StrategyEnum.DOUBLE_EMA_WITH_BOLLINGER))

                val backTestReport = doubleEmaWithBollingerScalpingStrategy.backTestWithChart(1L, reportSize)

                val results =
                    backTestReport
                        .tradingRecords
                        .map { (_, tradingRecord) ->
                            tradingRecord
                                .positions.sumOf { pos ->
                                    pos.profit.doubleValue()
                                }

                        }.sum()

                sum += results

                FileOutputStream(File(outputFileName)).use { stream ->
                    ChartUtils.writeChartAsPNG(
                        stream,
                        backTestReport.chart,
                        1920,
                        600
                    )
                }
            }

        println(sum)
    }
}