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
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import ru.topskiy.personalassistant.ui.theme.CatalogCardBgDark
import ru.topskiy.personalassistant.ui.theme.CatalogCardBgLight
import ru.topskiy.personalassistant.ui.theme.CatalogCardBorderDark
import ru.topskiy.personalassistant.ui.theme.CatalogCardBorderLight
import ru.topskiy.personalassistant.ui.theme.CatalogIconDark
import ru.topskiy.personalassistant.ui.theme.CatalogIconLight
import ru.topskiy.personalassistant.ui.theme.CatalogListDividerDark
import ru.topskiy.personalassistant.ui.theme.CatalogListDividerLight
import ru.topskiy.personalassistant.ui.theme.CatalogSectionHeaderDark
import ru.topskiy.personalassistant.ui.theme.CatalogSectionHeaderLight
import ru.topskiy.personalassistant.ui.theme.SwitchThumbChecked
import ru.topskiy.personalassistant.ui.theme.SwitchTrackCheckedGreen

private const val CATALOG_CARD_CORNER_DP = 12f
private val CATALOG_CARD_PADDING_DP = 4.dp
private val CATALOG_CARD_INNER_PADDING_DP = 12.dp
private val CATALOG_LIST_ROW_START_INSET_DP = 56.dp
private val CATALOG_LIST_ROW_PADDING_H = 16.dp
private val CATALOG_LIST_ROW_PADDING_V = 12.dp
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

/** Одна строка списка: иконка, название, переключатель. */
@Composable
fun ServiceCatalogListRow(
    service: AppService,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    darkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CATALOG_LIST_ROW_PADDING_H, vertical = CATALOG_LIST_ROW_PADDING_V),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = service.icon,
            contentDescription = stringResource(service.titleResId),
            modifier = Modifier.size(24.dp),
            tint = if (darkTheme) CatalogIconDark else CatalogIconLight
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(service.titleResId),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
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

/** Карточка сервиса для сетки: иконка, название, переключатель. */
@Composable
fun ServiceCatalogCard(
    service: AppService,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    darkTheme: Boolean
) {
    val cardBg = if (darkTheme) CatalogCardBgDark else CatalogCardBgLight
    val borderColor = if (darkTheme) CatalogCardBorderDark else CatalogCardBorderLight
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CATALOG_CARD_PADDING_DP),
        shape = RoundedCornerShape(CATALOG_CARD_CORNER_DP.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CATALOG_CARD_INNER_PADDING_DP),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = service.icon,
                contentDescription = stringResource(service.titleResId),
                modifier = Modifier.size(28.dp),
                tint = if (darkTheme) CatalogIconDark else CatalogIconLight
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(service.titleResId),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            CatalogSwitch(
                checked = enabled,
                onCheckedChange = onToggle,
                testTag = "service_${service.id}",
                modifier = Modifier.size(width = 36.dp, height = 24.dp)
            )
        }
    }
}

/** Список сервисов по категориям (вертикальный список с заголовками и строками). */
@Composable
fun ServiceCatalogListView(
    enabledIds: Set<ServiceId>,
    onToggle: (ServiceId, Boolean) -> Unit,
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
                        ServiceCatalogListRow(
                            service = service,
                            enabled = service.id in enabledIds,
                            onToggle = { onToggle(service.id, it) },
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
    onToggle: (ServiceId, Boolean) -> Unit,
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
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceCatalogCard(
                            service = service,
                            enabled = service.id in enabledIds,
                            onToggle = { onToggle(service.id, it) },
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
