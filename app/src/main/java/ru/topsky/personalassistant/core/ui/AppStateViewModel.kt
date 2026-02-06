package ru.topsky.personalassistant.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.topsky.personalassistant.core.datastore.SettingsRepository
import ru.topsky.personalassistant.core.model.ServiceId

data class AppStateUiState(
    val enabledServices: Set<ServiceId>,
    val favoriteService: ServiceId?,
    val lastService: ServiceId?,
    val onboardingDone: Boolean
)

class AppStateViewModel(
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

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    fun toggleService(serviceId: ServiceId, enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.enabledServices
            if (enabled) {
                settingsRepository.setEnabledServices(current + serviceId)
            } else {
                if (current.size <= 1) {
                    _messageEvent.emit("В приложении должен быть включён хотя бы один сервис")
                    return@launch
                }
                settingsRepository.setEnabledServices(current - serviceId)
            }
        }
    }

    fun setFavorite(serviceId: ServiceId?) {
        viewModelScope.launch {
            settingsRepository.setFavorite(serviceId)
        }
    }

    fun setLastService(serviceId: ServiceId?) {
        viewModelScope.launch {
            settingsRepository.setLastService(serviceId)
        }
    }

    fun setOnboardingDone(done: Boolean) {
        viewModelScope.launch {
            settingsRepository.setOnboardingDone(done)
        }
    }

    fun setEnabledServicesDirectly(services: Set<ServiceId>) {
        viewModelScope.launch {
            settingsRepository.setEnabledServices(services)
        }
    }

    fun setTheme(mode: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(mode)
        }
    }
}
