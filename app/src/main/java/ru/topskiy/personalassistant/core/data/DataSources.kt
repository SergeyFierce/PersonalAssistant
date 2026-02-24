package ru.topskiy.personalassistant.core.data

/**
 * Единый тип результата для операций репозиториев.
 *
 * На текущем этапе это просто typealias к [Result], но наличие отдельного типа
 * позволяет при необходимости заменить реализацию (например, на собственный
 * sealed class ошибок) без переписывания всех сигнатур.
 */
typealias RepositoryResult<T> = Result<T>

/**
 * Базовый каркас для локальных источников данных (БД, DataStore, SharedPreferences, файлы).
 *
 * Этот интерфейс задаёт общий подход для будущих доменов:
 * - операции не бросают исключения наружу, а возвращают [Result];
 * - API явно разделяет чтение/запись/удаление по ключу.
 *
 * Конкретные реализации могут расширять этот интерфейс или использовать его как ориентир
 * при проектировании собственных методов.
 */
interface LocalDataSource<in Key, Entity> {

    /**
     * Возвращает сущность по ключу или null, если она отсутствует.
     */
    suspend fun getByKey(key: Key): RepositoryResult<Entity?>

    /**
     * Сохраняет значение по указанному ключу.
     */
    suspend fun put(key: Key, value: Entity): RepositoryResult<Unit>

    /**
     * Удаляет значение по ключу (если оно существует).
     */
    suspend fun remove(key: Key): RepositoryResult<Unit>
}

/**
 * Базовый каркас для удалённых источников данных (HTTP API и т.п.).
 *
 * В будущих доменах поверх этого интерфейса можно строить конкретные клиенты
 * (например, Retrofit/Ktor), сохраняя единый контракт: входной Request-модель
 * и выходной Response-модель, обёрнутые в [Result].
 */
interface RemoteDataSource<Request, Response> {

    /**
     * Выполняет удалённый запрос и возвращает результат.
     */
    suspend fun execute(request: Request): RepositoryResult<Response>
}

