package ru.topskiy.personalassistant.core.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.core.datastore.InitialSettings
import ru.topskiy.personalassistant.core.datastore.SettingsRepository
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.core.model.ServiceRegistry
import javax.inject.Inject

fun AppStateUiState.homeServiceId(): ServiceId {
    val enabled = enabledServices
    favoriteService?.takeIf { it in enabled }?.let { return it }
    lastService?.takeIf { it in enabled }?.let { return it }
    return ServiceRegistry.all.firstOrNull { it.id in enabled }?.id ?: ServiceId.DEALS
}

data class AppStateUiState(
    val enabledServices: Set<ServiceId>,
    val favoriteService: ServiceId?,
    val lastService: ServiceId?,
    val onboardingDone: Boolean
)

@HiltViewModel
class AppStateViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState = combine(
        settingsRepository.enabledServicesFlow,
        settingsRepository.favoriteServiceFlow,
        settingsRepository.lastServiceFlow,
        settingsRepository.onboardingDoneFlow
    ) { enabledServices, favoriteService, lastService, onboardingDone ->
        AppStateUiState(
            enabledServices = enabledServices,
            favoriteService = favoriteService,
            lastService = lastService,
            onboardingDone = onboardingDone
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppStateUiState(
            enabledServices = emptySet(),
            favoriteService = null,
            lastService = null,
            onboardingDone = false
        )
    )

    val themeMode = settingsRepository.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = "system"
    )

    /** null = ещё не загружено (не рисовать каталог), true = список, false = карточки. */
    val loadedCatalogViewMode = settingsRepository.servicesCatalogListViewFlow
        .map { it as Boolean? }
        .onStart { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun setServicesCatalogListView(listView: Boolean) {
        viewModelScope.launch {
            settingsRepository.setServicesCatalogListView(listView).onFailure {
                _messageEvent.emit(R.string.settings_save_error)
            }
        }
    }

    private val _messageEvent = MutableSharedFlow<Int>()
    val messageEvent: SharedFlow<Int> = _messageEvent.asSharedFlow()

    fun toggleService(serviceId: ServiceId, enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.enabledServices
            if (enabled) {
                settingsRepository.setEnabledServices(current + serviceId).onFailure {
                    _messageEvent.emit(R.string.settings_save_error)
                }
            } else {
                if (current.size <= 1) {
                    _messageEvent.emit(R.string.min_one_service_required)
                    return@launch
                }
                settingsRepository.setEnabledServices(current - serviceId).onFailure {
                    _messageEvent.emit(R.string.settings_save_error)
                }
            }
        }
    }

    fun setFavorite(serviceId: ServiceId?) {
        viewModelScope.launch {
            settingsRepository.setFavorite(serviceId).onFailure {
                _messageEvent.emit(R.string.settings_save_error)
            }
        }
    }

    fun setLastService(serviceId: ServiceId?) {
        viewModelScope.launch {
            settingsRepository.setLastService(serviceId).onFailure {
                _messageEvent.emit(R.string.settings_save_error)
            }
        }
    }

    fun setOnboardingDone(done: Boolean) {
        viewModelScope.launch {
            settingsRepository.setOnboardingDone(done).onFailure {
                _messageEvent.emit(R.string.settings_save_error)
            }
        }
    }

    fun setEnabledServicesDirectly(services: Set<ServiceId>) {
        viewModelScope.launch {
            settingsRepository.setEnabledServices(services).onFailure {
                _messageEvent.emit(R.string.settings_save_error)
            }
        }
    }

    /** Сохраняет результат онбординга в DataStore (все три записи подряд). Вызывать перед навигацией с онбординга. */
    suspend fun completeOnboarding(selectedServices: Set<ServiceId>, firstService: ServiceId): Result<Unit> {
        settingsRepository.setEnabledServices(selectedServices).onFailure {
            _messageEvent.emit(R.string.settings_save_error)
            return Result.failure(it)
        }
        settingsRepository.setOnboardingDone(true).onFailure {
            _messageEvent.emit(R.string.settings_save_error)
            return Result.failure(it)
        }
        settingsRepository.setLastService(firstService).onFailure {
            _messageEvent.emit(R.string.settings_save_error)
            return Result.failure(it)
        }
        return Result.success(Unit)
    }

    fun setTheme(mode: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(mode).onFailure {
                _messageEvent.emit(R.string.settings_save_error)
            }
        }
    }

    /** Читает сохранённое состояние из DataStore для первой навигации (избегаем показа онбординга до загрузки). При ошибке — дефолт и messageEvent. */
    suspend fun getInitialState(): AppStateUiState {
        val defaultSettings = InitialSettings(
            enabledServices = setOf(ServiceId.DEALS),
            favoriteService = null,
            lastService = null,
            onboardingDone = false
        )
        val s = settingsRepository.getInitialSettings().getOrElse { e ->
            Log.e("AppStateViewModel", "getInitialState: load failed, using defaults", e)
            _messageEvent.emit(R.string.settings_load_error)
            defaultSettings
        }
        return AppStateUiState(
            enabledServices = s.enabledServices,
            favoriteService = s.favoriteService,
            lastService = s.lastService,
            onboardingDone = s.onboardingDone
        )
    }
}
