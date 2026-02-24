package ru.topskiy.personalassistant.core.data.remote

import ru.topskiy.personalassistant.core.data.RepositoryResult

/**
 * Базовый интерфейс HTTP‑клиента для будущих сетевых запросов.
 *
 * Конкретная реализация (Retrofit + OkHttp, Ktor и т.п.) скрывается за этим интерфейсом,
 * чтобы UI и доменный слой не зависели от выбора библиотеки.
 */
interface NetworkClient {

    /**
     * Выполняет HTTP‑запрос и возвращает доменную модель [T] или ошибку.
     *
     * На уровне реализации здесь будет:
     * - построение запроса (URL, метод, заголовки, тело);
     * - вызов Retrofit/Ktor/OkHttp;
     * - маппинг ошибок (сетевые/HTTP/десериализация) в [NetworkError];
     * - обёртка результата в [RepositoryResult].
     */
    suspend fun <T> execute(request: NetworkRequest<T>): RepositoryResult<T>
}

/**
 * Минимальная модель запроса, не завязанная на конкретную HTTP‑библиотеку.
 *
 * В реальной реализации может быть расширена (заголовки, query‑параметры и т.д.)
 * или заменена на более специализированные типы.
 */
data class NetworkRequest<T>(
    val endpointId: String,
    val description: String? = null
)

/**
 * Базовое описание сетевых ошибок, чтобы централизованно логировать и маппить их в UI.
 *
 * Пока нигде не используется, но при добавлении настоящего сетевого слоя [RepositoryResult]
 * может быть переписан на sealed‑класс, использующий [NetworkError] для удалённых операций.
 */
sealed interface NetworkError {
    data class Http(val code: Int, val bodySnippet: String?) : NetworkError
    object NetworkUnavailable : NetworkError
    object Timeout : NetworkError
    data class Serialization(val message: String?) : NetworkError
    data class Unknown(val throwable: Throwable?) : NetworkError
}

