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
        val folder = "C:\\progs\\local\\data"

        File(folder)
            .listFiles()
            ?.forEach {
                //Путь выходного файла
                val outputFileName = "C:\\progs\\local\\graphics\\chart_${it.name}.png"
                //Количество свечей с конца, по которым будет создан график
                val reportSize = 100

                val marketData = MarketDataFileMockService(it.absolutePath)

                val doubleEmaWithBollingerScalpingStrategy = DoubleEmaWithBollingerScalpingStrategy(marketData)
                doubleEmaWithBollingerScalpingStrategy.startProcessing(1, CompanyStrategy("", StrategyEnum.SIMPLE_SCALPING))

                val chart = doubleEmaWithBollingerScalpingStrategy.prepareChart(1L, reportSize)
                FileOutputStream(File(outputFileName)).use { stream ->
                    ChartUtils.writeChartAsPNG(
                        stream,
                        chart,
                        1920,
                        600
                    )
                }
            }

    }
}