package ru.devyandex.investmenthelper.service.core

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.devyandex.investmenthelper.service.core.apiclient.InvestApiClientProvider

@Service
class UserService(
    private val apiClientProvider: InvestApiClientProvider
) {

    /**
     * Валидация токена и создание/обновление пользователя
     * @param id - Уникальный идентификатор пользователя
     * @param token - Токен для InvestApi
     *
     * @return Результат проверки токена. True если токен валиден, иначе false
     */
    fun authenticateToken(id: Long, token: String): Boolean =
        try {
            val client = apiClientProvider.upsertClient(id, token)

            client.apiClient.userService.infoSync

            true
        } catch (ex: Exception) {
            logger.error { "Ошибка при валидации токена: ${ex.message}" }
            false
        }

    /**
     * Получение пользователя
     * @param id - Уникальный идентификатор пользователя
     *
     * @return ApiClientDto - ДТО с основными параметрами пользователя (включая экземпляр API)
     */
    fun getClientById(id: Long) = apiClientProvider.getClient(id)

    /**
     * Удаление пользователя
     * @param id - Уникальный идентификатор пользователя
     */
    fun removeClient(id: Long) = apiClientProvider.removeClient(id)

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
