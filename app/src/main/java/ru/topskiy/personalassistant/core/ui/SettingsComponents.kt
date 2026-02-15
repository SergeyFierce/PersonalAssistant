package ru.topskiy.personalassistant.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.ui.theme.CatalogCardBgDark
import ru.topskiy.personalassistant.ui.theme.CatalogCardBgLight
import ru.topskiy.personalassistant.ui.theme.CatalogListDividerDark
import ru.topskiy.personalassistant.ui.theme.CatalogListDividerLight
import ru.topskiy.personalassistant.ui.theme.CatalogSectionHeaderDark
import ru.topskiy.personalassistant.ui.theme.CatalogSectionHeaderLight
import ru.topskiy.personalassistant.ui.theme.SwitchThumbChecked
import ru.topskiy.personalassistant.ui.theme.SwitchTrackCheckedGreen

private val GROUP_CORNER_RADIUS = 10.dp
private val ROW_VERTICAL_PADDING = 11.dp
private val ROW_HORIZONTAL_PADDING = 16.dp
private val GROUP_HORIZONTAL_PADDING = 16.dp
private val GROUP_VERTICAL_SPACING = 20.dp

/**
 * Группа строк в стиле iOS: скруглённый блок на сером фоне (CatalogCardBg).
 */
@Composable
fun SettingsGroup(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val groupBg = if (darkTheme) CatalogCardBgDark else CatalogCardBgLight
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = GROUP_HORIZONTAL_PADDING, vertical = GROUP_VERTICAL_SPACING / 2)
    ) {
        Surface(
            shape = RoundedCornerShape(GROUP_CORNER_RADIUS),
            color = groupBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

/**
 * Строка с заголовком и стрелкой вправо (переход на подэкран), стиль iOS.
 */
@Composable
fun SettingsRow(
    title: String,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    value: String? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    val titleColor = MaterialTheme.colorScheme.onSurface
    val valueColor = if (darkTheme) CatalogSectionHeaderDark else CatalogSectionHeaderLight
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ROW_HORIZONTAL_PADDING, vertical = ROW_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = valueColor,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.content_description_chevron),
                    tint = valueColor
                )
            }
        }
    }
}

/**
 * Строка с переключателем как в каталоге сервисов (зелёный track, белый thumb).
 */
@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val titleColor = MaterialTheme.colorScheme.onSurface
    val subtitleColor = if (darkTheme) CatalogSectionHeaderDark else CatalogSectionHeaderLight
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = ROW_HORIZONTAL_PADDING, vertical = ROW_VERTICAL_PADDING)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SwitchThumbChecked,
                    checkedTrackColor = SwitchTrackCheckedGreen,
                    checkedBorderColor = SwitchTrackCheckedGreen
                )
            )
        }
    }
}

/**
 * Разделитель между строками внутри группы (стиль iOS / каталог).
 */
@Composable
fun SettingsRowDivider(darkTheme: Boolean) {
    val dividerColor = if (darkTheme) CatalogListDividerDark else CatalogListDividerLight
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = ROW_HORIZONTAL_PADDING),
        color = dividerColor,
        thickness = 1.dp
    )
}
