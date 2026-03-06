package ru.topskiy.personalassistant.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import ru.topskiy.personalassistant.R
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

@Composable
fun DrawerContent(
    navController: NavHostController,
    uiState: AppStateUiState,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onItemClickThenCloseDrawer: (afterClose: () -> Unit) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    ModalDrawerSheet(
        modifier = Modifier.width(320.dp).fillMaxHeight(),
        drawerShape = RoundedCornerShape(0.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
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
                            contentDescription = stringResource(R.string.content_description_profile),
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.day_night)
                )
                val themeToggleColor = if (darkTheme) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                val themeColorFilter = remember(themeToggleColor) {
                    PorterDuffColorFilter(
                        android.graphics.Color.argb(
                            (themeToggleColor.alpha * 255).toInt(),
                            (themeToggleColor.red * 255).toInt(),
                            (themeToggleColor.green * 255).toInt(),
                            (themeToggleColor.blue * 255).toInt()
                        ),
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
                val lottieDynamicProperties = rememberLottieDynamicProperties(
                    rememberLottieDynamicProperty(
                        property = LottieProperty.COLOR_FILTER,
                        value = themeColorFilter,
                        *arrayOf("**")
                    )
                )
                val themeProgress by animateFloatAsState(
                    targetValue = if (darkTheme) 0f else 0.5f,
                    animationSpec = tween(
                        durationMillis = THEME_ANIMATION_DURATION_MS,
                        easing = FastOutSlowInEasing
                    ),
                    label = "theme_lottie"
                )
                val themeToggleContentDesc = if (darkTheme) stringResource(R.string.theme_switch_to_light) else stringResource(R.string.theme_switch_to_dark)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleTheme)
                        .semantics { contentDescription = themeToggleContentDesc },
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { themeProgress },
                        modifier = Modifier.size(32.dp),
                        dynamicProperties = lottieDynamicProperties
                    )
                }
            }
            val drawerItemUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    val isServicesSelected = currentRoute == MANAGE_SERVICES_ROUTE
                    val isSettingsSelected = currentRoute?.startsWith("settings") == true

                    Box(modifier = Modifier.clip(RoundedCornerShape(0.dp))) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Apps, contentDescription = stringResource(R.string.drawer_services)) },
                            label = { Text(stringResource(R.string.drawer_services), modifier = Modifier.padding(start = 24.dp)) },
                            selected = isServicesSelected,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = drawerItemUnselectedColor,
                                unselectedTextColor = drawerItemUnselectedColor
                            ),
                            onClick = {
                                onItemClickThenCloseDrawer {
                                    if (currentRoute != MANAGE_SERVICES_ROUTE) {
                                        navController.navigate(MANAGE_SERVICES_ROUTE) { launchSingleTop = true }
                                    }
                                }
                            }
                        )
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(0.dp))) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.drawer_settings)) },
                            label = { Text(stringResource(R.string.drawer_settings), modifier = Modifier.padding(start = 24.dp)) },
                            selected = isSettingsSelected,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = drawerItemUnselectedColor,
                                unselectedTextColor = drawerItemUnselectedColor
                            ),
                            onClick = {
                                onItemClickThenCloseDrawer {
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
