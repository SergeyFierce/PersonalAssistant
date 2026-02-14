package ru.topskiy.personalassistant.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.topskiy.personalassistant.core.model.AppService
import ru.topskiy.personalassistant.core.model.ServiceCategory
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.core.model.ServiceRegistry
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.ui.theme.CatalogCardBgDark
import ru.topskiy.personalassistant.ui.theme.CatalogCardBgLight
import ru.topskiy.personalassistant.ui.theme.CatalogCardBorderDark
import ru.topskiy.personalassistant.ui.theme.CatalogCardBorderLight
import ru.topskiy.personalassistant.ui.theme.CatalogIconDark
import ru.topskiy.personalassistant.ui.theme.CatalogIconLight
import ru.topskiy.personalassistant.ui.theme.FavoriteStarEmpty
import ru.topskiy.personalassistant.ui.theme.FavoriteStarYellow
import ru.topskiy.personalassistant.ui.theme.CatalogListDividerDark
import ru.topskiy.personalassistant.ui.theme.CatalogListDividerLight
import ru.topskiy.personalassistant.ui.theme.CatalogSectionHeaderDark
import ru.topskiy.personalassistant.ui.theme.CatalogSectionHeaderLight
import ru.topskiy.personalassistant.ui.theme.SwitchThumbChecked
import ru.topskiy.personalassistant.ui.theme.SwitchTrackCheckedGreen

private const val CATALOG_CARD_CORNER_DP = 16f
private val CATALOG_CARD_PADDING_DP = 4.dp
private val CATALOG_CARD_INNER_PADDING_DP = 16.dp
private val CATALOG_CARD_ICON_SIZE_DP = 34.dp
private val CATALOG_CARD_ICON_CIRCLE_SIZE_DP = 48.dp
private val CATALOG_CARD_STAR_SIZE_DP = 24.dp
private val CATALOG_LIST_ROW_START_INSET_DP = 56.dp
private val CATALOG_LIST_ROW_PADDING_H = 16.dp
private val CATALOG_LIST_ROW_PADDING_V = 10.dp
private val CATALOG_LIST_ICON_CIRCLE_SIZE_DP = 40.dp
private val CATALOG_LIST_ICON_SIZE_DP = 28.dp
private val CATALOG_GRID_ROW_SPACING_DP = 8.dp

/** Заголовок секции каталога сервисов (название категории). */
@Composable
fun ServiceCatalogSectionHeader(
    title: String,
    darkTheme: Boolean
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = if (darkTheme) CatalogSectionHeaderDark else CatalogSectionHeaderLight,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, end = 4.dp, bottom = 6.dp),
        fontWeight = FontWeight.Medium
    )
}

/** Одна строка списка: иконка в круге (как в карточках), название, звёздочка избранного, переключатель. */
@Composable
fun ServiceCatalogListRow(
    service: AppService,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    isFavorite: Boolean,
    onSetFavorite: (() -> Unit)?,
    darkTheme: Boolean
) {
    val iconTint = if (darkTheme) CatalogIconDark else CatalogIconLight
    val starTint = if (isFavorite) FavoriteStarYellow else FavoriteStarEmpty
    val iconCircleBg = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val contentAlpha = if (enabled) 1f else 0.5f
    val starButtonBg = if (isFavorite) FavoriteStarYellow.copy(alpha = 0.15f) else (if (darkTheme) CatalogCardBgDark else CatalogCardBgLight)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CATALOG_LIST_ROW_PADDING_H, vertical = CATALOG_LIST_ROW_PADDING_V),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { alpha = contentAlpha }
                .size(CATALOG_LIST_ICON_CIRCLE_SIZE_DP)
                .background(iconCircleBg, RoundedCornerShape(999.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = service.icon,
                contentDescription = stringResource(service.titleResId),
                modifier = Modifier.size(CATALOG_LIST_ICON_SIZE_DP),
                tint = iconTint
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(service.titleResId),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer { alpha = contentAlpha }
        )
        if (enabled && onSetFavorite != null) {
            IconButton(
                onClick = { onSetFavorite() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = starButtonBg,
                    contentColor = starTint
                )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.content_description_remove_favorite
                        else R.string.content_description_set_favorite
                    ),
                    modifier = Modifier.size(CATALOG_CARD_STAR_SIZE_DP)
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        CatalogSwitch(
            checked = enabled,
            onCheckedChange = onToggle,
            testTag = "service_${service.id}"
        )
    }
}

/** Переключатель включения сервиса (общие цвета для списка и сетки). */
@Composable
private fun CatalogSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.testTag(testTag),
        colors = SwitchDefaults.colors(
            checkedThumbColor = SwitchThumbChecked,
            checkedTrackColor = SwitchTrackCheckedGreen,
            checkedBorderColor = SwitchTrackCheckedGreen
        )
    )
}

/** Карточка сервиса для сетки: иконка в круге, название, переключатель; звёздочка избранного сверху справа. */
@Composable
fun ServiceCatalogCard(
    service: AppService,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    isFavorite: Boolean,
    onSetFavorite: (() -> Unit)?,
    darkTheme: Boolean
) {
    val cardBg = if (darkTheme) CatalogCardBgDark else CatalogCardBgLight
    val borderColor = if (darkTheme) CatalogCardBorderDark else CatalogCardBorderLight
    val iconTint = if (darkTheme) CatalogIconDark else CatalogIconLight
    val starTint = if (isFavorite) FavoriteStarYellow else FavoriteStarEmpty
    val iconCircleBg = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val contentAlpha = if (enabled) 1f else 0.5f
    val starButtonBg = if (isFavorite) FavoriteStarYellow.copy(alpha = 0.15f) else cardBg

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CATALOG_CARD_PADDING_DP),
        shape = RoundedCornerShape(CATALOG_CARD_CORNER_DP.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CATALOG_CARD_INNER_PADDING_DP),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = contentAlpha }
                        .size(CATALOG_CARD_ICON_CIRCLE_SIZE_DP)
                        .background(iconCircleBg, RoundedCornerShape(999.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = service.icon,
                        contentDescription = stringResource(service.titleResId),
                        modifier = Modifier.size(CATALOG_CARD_ICON_SIZE_DP),
                        tint = iconTint
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(service.titleResId),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { alpha = contentAlpha }
                )
                Spacer(modifier = Modifier.height(10.dp))
                CatalogSwitch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    testTag = "service_${service.id}",
                    modifier = Modifier.size(width = 36.dp, height = 24.dp)
                )
            }
            if (enabled && onSetFavorite != null) {
                IconButton(
                    onClick = { onSetFavorite() },
                    modifier = Modifier.align(Alignment.TopEnd),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = starButtonBg,
                        contentColor = starTint
                    )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = stringResource(
                            if (isFavorite) R.string.content_description_remove_favorite
                            else R.string.content_description_set_favorite
                        ),
                        modifier = Modifier.size(CATALOG_CARD_STAR_SIZE_DP)
                    )
                }
            }
        }
    }
}

/** Список сервисов по категориям (вертикальный список с заголовками и строками). */
@Composable
fun ServiceCatalogListView(
    enabledIds: Set<ServiceId>,
    favoriteServiceId: ServiceId?,
    onToggle: (ServiceId, Boolean) -> Unit,
    onSetFavorite: (ServiceId?) -> Unit,
    darkTheme: Boolean
) {
    val groupShape = RoundedCornerShape(CATALOG_CARD_CORNER_DP.dp)
    val cardBg = if (darkTheme) CatalogCardBgDark else CatalogCardBgLight
    val dividerColor = if (darkTheme) CatalogListDividerDark else CatalogListDividerLight

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        ServiceRegistry.groupedByCategory.forEach { (category, services) ->
            ServiceCatalogSectionHeader(stringResource(category.titleResId), darkTheme)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = groupShape,
                color = cardBg,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = BorderStroke(0.5.dp, if (darkTheme) CatalogCardBorderDark else CatalogCardBorderLight)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    services.forEachIndexed { index, service ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = CATALOG_LIST_ROW_START_INSET_DP),
                                color = dividerColor,
                                thickness = 0.5.dp
                            )
                        }
                        val isFavorite = service.id == favoriteServiceId
                        ServiceCatalogListRow(
                            service = service,
                            enabled = service.id in enabledIds,
                            onToggle = { onToggle(service.id, it) },
                            isFavorite = isFavorite,
                            onSetFavorite = { onSetFavorite(if (isFavorite) null else service.id) },
                            darkTheme = darkTheme
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Сетка карточек сервисов (несколько колонок). */
@Composable
fun ServiceCatalogGrid(
    services: List<AppService>,
    columnCount: Int,
    enabledIds: Set<ServiceId>,
    favoriteServiceId: ServiceId?,
    onToggle: (ServiceId, Boolean) -> Unit,
    onSetFavorite: (ServiceId?) -> Unit,
    darkTheme: Boolean
) {
    val rows = services.chunked(columnCount)
    Column(verticalArrangement = Arrangement.spacedBy(CATALOG_GRID_ROW_SPACING_DP)) {
        rows.forEach { rowServices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CATALOG_GRID_ROW_SPACING_DP)
            ) {
                rowServices.forEach { service ->
                    val isFavorite = service.id == favoriteServiceId
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceCatalogCard(
                            service = service,
                            enabled = service.id in enabledIds,
                            onToggle = { onToggle(service.id, it) },
                            isFavorite = isFavorite,
                            onSetFavorite = { onSetFavorite(if (isFavorite) null else service.id) },
                            darkTheme = darkTheme
                        )
                    }
                }
                repeat(columnCount - rowServices.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
