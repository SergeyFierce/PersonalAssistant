package ru.topsky.personalassistant.core.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import ru.topsky.personalassistant.core.model.AppService
import ru.topsky.personalassistant.core.model.ServiceId
import ru.topsky.personalassistant.core.model.ServiceRegistry
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.yield
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.abs

const val BOOTSTRAP_ROUTE = "bootstrap"
const val ONBOARDING_ROUTE = "onboarding"
const val MAIN_ROUTE = "main"
const val MANAGE_SERVICES_ROUTE = "manage_services"
const val SETTINGS_ROUTE = "settings"

private const val DRAWER_DRAG_THRESHOLD_PX = 40f

private fun Modifier.drawerOpenGestureInTopBar(onOpenDrawer: () -> Unit): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        val edgePx = with(density) { 64.dp.toPx() }
        var dragStartX = 0f
        detectHorizontalDragGestures(
            onDragStart = { offset -> dragStartX = offset.x },
            onHorizontalDrag = { _, dragAmount ->
                if (dragStartX <= edgePx && dragAmount > DRAWER_DRAG_THRESHOLD_PX) {
                    onOpenDrawer()
                }
            }
        )
    }
)

private fun AppStateUiState.homeServiceId(): ServiceId {
    val enabled = enabledServices

    favoriteService?.takeIf { it in enabled }?.let { return it }
    lastService?.takeIf { it in enabled }?.let { return it }

    return ServiceRegistry.all.firstOrNull { it.id in enabled }?.id ?: ServiceId.DEALS
}

private fun AppStateUiState.homeRoute(): String = MAIN_ROUTE

private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    uiState: AppStateUiState,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onCloseDrawerAndThen: (afterClose: () -> Unit) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    ModalDrawerSheet(
        modifier = Modifier.width(320.dp).fillMaxHeight(),
        drawerShape = RoundedCornerShape(0.dp),
        // Цвет фона бокового меню (шапка): тёмная #1B1B1B, светлая #5A8FBB
        drawerContainerColor = if (darkTheme) Color(0xFF1B1B1B) else Color(0xFF5A8FBB)
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            // Шапка: аватар слева, кнопка темы в самом правом верхнем углу
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                // Аватар слева с отступами
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Кнопка смены темы: вращение 180°, схлопывание в середине, смена иконки при ~90°
                val rotation by animateFloatAsState(
                    targetValue = if (darkTheme) 0f else 180f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                    label = "theme_icon_rotation"
                )
                val scale = if (rotation <= 90f) 1f - (rotation / 90f) * 0.8f
                else 0.2f + ((rotation - 90f) / 90f) * 0.8f
                val iconToDisplay = if (rotation > 90f) Icons.Outlined.DarkMode else Icons.Outlined.LightMode
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleTheme),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconToDisplay,
                        contentDescription = if (darkTheme) "Включить светлую тему" else "Включить тёмную тему",
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer(
                                rotationY = rotation,
                                scaleX = scale,
                                scaleY = scale
                            ),
                        tint = Color.White
                    )
                }
            }
            // Всё что под шапкой: тёмная #262427, светлая #FFFFFF. До самого низа, без синей полосы.
            val drawerItemColor = if (darkTheme) MaterialTheme.colorScheme.onSurface else Color.Black
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (darkTheme) Color(0xFF262427) else Color(0xFFFFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(0.dp))) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Apps, contentDescription = null) },
                            label = { Text("Сервисы", modifier = Modifier.padding(start = 24.dp)) },
                            selected = false,
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedIconColor = drawerItemColor,
                                unselectedTextColor = drawerItemColor
                            ),
                            onClick = {
                                onCloseDrawerAndThen {
                                    if (currentRoute != MANAGE_SERVICES_ROUTE) {
                                        navController.navigate(MANAGE_SERVICES_ROUTE) { launchSingleTop = true }
                                    }
                                }
                            }
                        )
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(0.dp))) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                            label = { Text("Настройки", modifier = Modifier.padding(start = 24.dp)) },
                            selected = false,
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedIconColor = drawerItemColor,
                                unselectedTextColor = drawerItemColor
                            ),
                            onClick = {
                                onCloseDrawerAndThen {
                                    if (currentRoute != SETTINGS_ROUTE) {
                                        navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: AppStateViewModel,
    uiState: AppStateUiState,
    darkTheme: Boolean,
    onOpenDrawer: () -> Unit,
    drawerActive: Boolean,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val dockListState = rememberLazyListState()
    NavHost(
        navController = navController,
        startDestination = BOOTSTRAP_ROUTE,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(durationMillis = 250)
            )
        },
        exitTransition = {
            // При открытии Сервисы/Настройки основной экран остаётся под ними (без анимации)
            if (targetState.destination.route == MANAGE_SERVICES_ROUTE || targetState.destination.route == SETTINGS_ROUTE) {
                ExitTransition.None
            } else {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 250)
                )
            }
        },
        popEnterTransition = {
            // Возврат с Сервисы/Настройки: основной экран появляется слева направо
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = 250)
            )
        },
        popExitTransition = {
            // Закрытие экрана (Back): уходит вправо
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 250)
            )
        }
    ) {
        composable(
            route = BOOTSTRAP_ROUTE
        ) {
            BootstrapScreen(
                navController = navController,
                uiState = uiState
            )
        }

        composable(
            route = ONBOARDING_ROUTE
        ) {
            OnboardingScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = MAIN_ROUTE
        ) {
            ServicesMainScreen(
                navController = navController,
                viewModel = viewModel,
                uiState = uiState,
                darkTheme = darkTheme,
                onOpenDrawer = onOpenDrawer,
                drawerActive = drawerActive,
                scope = scope,
                dockListState = dockListState
            )
        }

        composable(
            route = MANAGE_SERVICES_ROUTE,
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                // Закрытие «Сервисы» (Back): плавное уход вправо, как в Telegram
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            }
        ) {
            ManageServicesScreen(
                navController = navController,
                viewModel = viewModel,
                uiState = uiState,
                darkTheme = darkTheme,
                onOpenDrawer = onOpenDrawer,
                drawerActive = drawerActive,
                scope = scope
            )
        }

        composable(
            route = SETTINGS_ROUTE,
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                // Закрытие «Настройки» (Back): плавное уход вправо, как в Telegram
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            }
        ) {
            SettingsScreen(
                navController = navController,
                viewModel = viewModel,
                uiState = uiState,
                darkTheme = darkTheme,
                onOpenDrawer = onOpenDrawer,
                drawerActive = drawerActive,
                scope = scope
            )
        }
    }
}

@Composable
fun BootstrapScreen(
    navController: NavHostController,
    uiState: AppStateUiState
) {
    LaunchedEffect(uiState.onboardingDone, uiState.favoriteService, uiState.lastService, uiState.enabledServices) {
        val targetRoute = if (!uiState.onboardingDone) {
            ONBOARDING_ROUTE
        } else {
            uiState.homeRoute()
        }
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute == targetRoute) {
            return@LaunchedEffect
        }
        navController.navigate(targetRoute) {
            popUpTo(BOOTSTRAP_ROUTE) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Загрузка…",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: AppStateViewModel
) {
    var selectedServices by remember { mutableStateOf(setOf<ServiceId>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "С чего начнём?",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Выберите сервисы, которые будут доступны в приложении. Это можно изменить позже.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        ServiceRegistry.all.forEach { service ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedServices = if (service.id in selectedServices) {
                            selectedServices - service.id
                        } else {
                            selectedServices + service.id
                        }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = service.id in selectedServices,
                    onCheckedChange = { checked ->
                        selectedServices = if (checked) {
                            selectedServices + service.id
                        } else {
                            selectedServices - service.id
                        }
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = service.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = service.titleRu,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Подсказка",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Вы можете выбрать избранный сервис — он будет открываться при запуске приложения.\nЕсли избранный сервис не выбран, приложение откроется на последнем использованном сервисе.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val firstSelected = ServiceRegistry.all
                    .firstOrNull { it.id in selectedServices }
                    ?.id
                if (firstSelected != null) {
                    viewModel.setEnabledServicesDirectly(selectedServices)
                    viewModel.setOnboardingDone(true)
                    viewModel.setLastService(firstSelected)
                    navController.navigate(BOOTSTRAP_ROUTE) {
                        popUpTo(ONBOARDING_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedServices.isNotEmpty()
        ) {
            Text(
                text = "Начать",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageServicesScreen(
    navController: NavHostController,
    viewModel: AppStateViewModel,
    uiState: AppStateUiState,
    darkTheme: Boolean,
    onOpenDrawer: () -> Unit,
    drawerActive: Boolean,
    scope: CoroutineScope
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BackHandler(enabled = !drawerActive) {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сервисы") },
                actions = {
                    Text(
                        text = "Включено: ${uiState.enabledServices.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (darkTheme) Color(0xFF242424) else Color(0xFF517DA2),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                modifier = Modifier.drawerOpenGestureInTopBar(onOpenDrawer)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (darkTheme) Color(0xFF181818) else Color(0xFFFFFFFF))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ServiceRegistry.all.forEach { service ->
                val enabled = service.id in uiState.enabledServices
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = service.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = service.titleRu,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = enabled,
                        onCheckedChange = { viewModel.toggleService(service.id, it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: AppStateViewModel,
    uiState: AppStateUiState,
    darkTheme: Boolean,
    onOpenDrawer: () -> Unit,
    drawerActive: Boolean,
    scope: CoroutineScope
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val themeOptions = listOf(
        "system" to "Системная",
        "light" to "Светлая",
        "dark" to "Тёмная"
    )

    BackHandler(enabled = !drawerActive) {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (darkTheme) Color(0xFF242424) else Color(0xFF517DA2),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                modifier = Modifier.drawerOpenGestureInTopBar(onOpenDrawer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (darkTheme) Color(0xFF181818) else Color(0xFFFFFFFF))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Тема",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            themeOptions.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setTheme(value) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == value,
                        onClick = { viewModel.setTheme(value) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesMainScreen(
    navController: NavHostController,
    viewModel: AppStateViewModel,
    uiState: AppStateUiState,
    darkTheme: Boolean,
    onOpenDrawer: () -> Unit,
    drawerActive: Boolean,
    scope: CoroutineScope,
    dockListState: LazyListState
) {
    val enabled = uiState.enabledServices
    val homeServiceId = uiState.homeServiceId()
    val currentServiceId = remember(uiState) {
        val last = uiState.lastService
        when {
            last != null && last in enabled -> last
            else -> homeServiceId
        }
    }

    var lastBackPressTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current

    BackHandler(enabled = !drawerActive) {
        if (currentServiceId == homeServiceId) {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2000L) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressTime = now
                Toast.makeText(context, "Нажмите ещё раз, чтобы выйти", Toast.LENGTH_SHORT).show()
            }
        } else {
            viewModel.setLastService(homeServiceId)
        }
    }

    val dockServices = ServiceRegistry.all.filter { it.id in enabled }
    val currentService = remember(currentServiceId) { ServiceRegistry.byId(currentServiceId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = currentService.titleRu,
                        label = "service_title"
                    ) { title ->
                        Text(title)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Меню")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (darkTheme) Color(0xFF242424) else Color(0xFF517DA2),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                modifier = Modifier.drawerOpenGestureInTopBar(onOpenDrawer)
            )
        },
        bottomBar = {
            if (dockServices.size > 1) {
                DockBar(
                    dockServices = dockServices,
                    currentServiceId = currentServiceId,
                    onSelectService = { id -> viewModel.setLastService(id) },
                    dockListState = dockListState
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (darkTheme) Color(0xFF181818) else Color(0xFFFFFFFF))
                .padding(16.dp)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentServiceId,
                label = "service_content"
            ) { serviceId ->
                val service = ServiceRegistry.byId(serviceId)
                Text(
                    text = "${service.titleRu}\n\nСервис в разработке",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



@Composable
private fun DockBar(
    dockServices: List<AppService>,
    currentServiceId: ServiceId,
    onSelectService: (ServiceId) -> Unit,
    dockListState: LazyListState
) {
    val cs = MaterialTheme.colorScheme
    val n = dockServices.size
    if (n <= 1) return

    // Настройки геометрии
    val itemWidth = 84.dp
    val itemSpacing = 12.dp


    // Сколько элементов считаем "влезает без скролла"
    val maxNoScroll = 4

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val pillShape = RoundedCornerShape(999.dp)

        Surface(
            shape = pillShape,
            color = cs.surface.copy(alpha = 0.82f),  // "стекло" (без blur)
            tonalElevation = 0.dp,
            shadowElevation = 18.dp,                  // мягче и "глубже", как iOS
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 10.dp)
                .clip(pillShape)                       // чтобы всё внутри тоже было "капсулой"
        ) {
            // iOS-стиль: тонкая обводка + лёгкая подсветка сверху
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.06f)) // subtle highlight
                    .padding(0.dp)
            ) {
                // тонкая "рамка" (hairline)
                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(1.dp) // толщина рамки
                ) {
                    // реальный фон капсулы внутри рамки
                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(cs.surface.copy(alpha = 0.70f))
                    ) {
                        // ВАЖНО: здесь оставляешь свой текущий if (n <= maxNoScroll) ... else ...
                        // НИЧЕГО не меняем внутри
                        if (n <= maxNoScroll) {
                            val leftCount = n / 2
                            val isEven = n % 2 == 0
                            val middle: AppService? = if (!isEven) dockServices[leftCount] else null

                            val left = dockServices.take(leftCount)
                            val right = dockServices.drop(leftCount + (if (isEven) 0 else 1))

                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
                            ) {
                                left.forEach { service ->
                                    DockItem(service, service.id == currentServiceId, itemWidth) {
                                        onSelectService(service.id)
                                    }
                                }

                                if (!isEven) {
                                    DockItem(middle!!, middle.id == currentServiceId, itemWidth) {
                                        onSelectService(middle.id)
                                    }
                                }

                                right.forEach { service ->
                                    DockItem(service, service.id == currentServiceId, itemWidth) {
                                        onSelectService(service.id)
                                    }
                                }
                            }
                        } else {
                            val density = androidx.compose.ui.platform.LocalDensity.current
                            val itemWidthPx = with(density) { itemWidth.toPx() }

                            var viewportWidthPx by remember { mutableStateOf(0f) }

                            LaunchedEffect(currentServiceId, dockServices, viewportWidthPx) {
                                val index = dockServices.indexOfFirst { it.id == currentServiceId }
                                if (index >= 0 && viewportWidthPx > 0f) {
                                    val centerOffsetPx = ((viewportWidthPx - itemWidthPx) / 2f).toInt()
                                    dockListState.animateScrollToItem(index, scrollOffset = -centerOffsetPx)
                                }
                            }

                            LazyRow(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .onSizeChanged { viewportWidthPx = it.width.toFloat() },
                                state = dockListState,
                                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(dockServices, key = { it.id }) { service ->
                                    DockItem(service, service.id == currentServiceId, itemWidth) {
                                        onSelectService(service.id)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockItem(
    service: AppService,
    selected: Boolean,
    width: Dp,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "dock_item_scale"
    )
    val bg by animateColorAsState(
        targetValue = if (selected) cs.primaryContainer else Color.Transparent,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "dock_item_bg"
    )
    val contentColor =
        if (selected) cs.onPrimaryContainer
        else cs.onSurfaceVariant.copy(alpha = 0.85f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(width)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Icon(
            imageVector = service.icon,
            contentDescription = service.titleRu,
            modifier = Modifier.size(24.dp),
            tint = contentColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = service.titleRu,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}