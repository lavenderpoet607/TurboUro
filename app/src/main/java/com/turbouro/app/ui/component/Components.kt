package com.turbouro.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbouro.app.model.CapabilityState
import com.turbouro.app.model.ThermalStatus
import com.turbouro.app.ui.theme.CardShape
import com.turbouro.app.ui.theme.ChipShape
import com.turbouro.app.ui.theme.LargeCardShape
import com.turbouro.app.ui.theme.PrimaryButtonShape
import com.turbouro.app.ui.theme.SmallButtonShape
import com.turbouro.app.ui.theme.TelemetryUnitStyle
import com.turbouro.app.ui.theme.TelemetryValueStyle
import com.turbouro.app.ui.theme.TurboUroTheme

@Composable
fun TurboCard(
    modifier: Modifier = Modifier,
    isLarge: Boolean = false,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = TurboUroTheme.colors
    val shape = if (isLarge) LargeCardShape else CardShape
    val borderModifier = Modifier.border(
        width = 1.dp,
        color = borderColor ?: colors.border,
        shape = shape
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
            .then(
                if (onClick != null) {
                    Modifier.clip(shape).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        role = Role.Button,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = shape,
        color = colors.surface,
        contentColor = colors.textPrimary,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    unit: String? = null,
    modifier: Modifier = Modifier,
    statusDotColor: Color? = null,
    subtitle: String? = null,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = TurboUroTheme.colors

    TurboCard(
        modifier = modifier,
        borderColor = borderColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = TurboUroTheme.colors.run {
                    androidx.compose.ui.text.TextStyle(
                        color = textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            )
            if (statusDotColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusDotColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = TelemetryValueStyle,
                color = colors.textPrimary
            )
            if (unit != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = TelemetryUnitStyle,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), ChipShape)
            .border(1.dp, color.copy(alpha = 0.35f), ChipShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    val colors = TurboUroTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = colors.textSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor ?: colors.textPrimary
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = TurboUroTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        if (actionText != null && onActionClick != null) {
            Box(
                modifier = Modifier
                    .clip(SmallButtonShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        role = Role.Button,
                        onClick = onActionClick
                    )
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accentPrimary
                )
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = TurboUroTheme.colors
    val backgroundColor = if (enabled) colors.accentPrimary else colors.border
    val contentColor = if (enabled) Color.Black else colors.textMuted

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(PrimaryButtonShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                role = Role.Button,
                onClick = onClick
            ),
        shape = PrimaryButtonShape,
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = TurboUroTheme.colors

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .border(1.dp, colors.border, PrimaryButtonShape)
            .clip(PrimaryButtonShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                role = Role.Button,
                onClick = onClick
            ),
        shape = PrimaryButtonShape,
        color = colors.surfaceElevated,
        contentColor = colors.textPrimary,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colors.textPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
fun CustomTurboIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null
) {
    val colors = TurboUroTheme.colors
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint ?: colors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    val colors = TurboUroTheme.colors
    val iconTint = accentColor ?: colors.accentPrimary

    TurboCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconTint.copy(alpha = 0.12f), SmallButtonShape)
                    .border(1.dp, iconTint.copy(alpha = 0.25f), SmallButtonShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CapabilityRow(
    title: String,
    state: CapabilityState,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val colors = TurboUroTheme.colors
    val stateColor = when (state) {
        CapabilityState.AVAILABLE -> colors.success
        CapabilityState.LIMITED -> colors.warning
        CapabilityState.REQUIRES_PERMISSION -> colors.warning
        CapabilityState.REQUIRES_SHIZUKU -> colors.warning
        CapabilityState.UNSUPPORTED -> colors.textMuted
        CapabilityState.ERROR -> colors.danger
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.symbol} ${state.displayText}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = stateColor
            )
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionButtonText: String = "Aktifkan Izin"
) {
    val colors = TurboUroTheme.colors

    TurboCard(
        modifier = modifier,
        borderColor = if (isGranted) colors.success.copy(alpha = 0.35f) else colors.warning.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )
            StatusChip(
                text = if (isGranted) "DIIZINKAN" else "DIBUTUHKAN",
                color = if (isGranted) colors.success else colors.warning
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = colors.textSecondary
        )

        if (!isGranted) {
            Spacer(modifier = Modifier.height(14.dp))
            SecondaryButton(
                text = actionButtonText,
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ThermalIndicator(
    status: ThermalStatus,
    temperatureC: Float?,
    modifier: Modifier = Modifier
) {
    val colors = TurboUroTheme.colors
    val indicatorColor = when (status) {
        ThermalStatus.COOL -> colors.info
        ThermalStatus.NORMAL -> colors.success
        ThermalStatus.WARM -> colors.warning
        ThermalStatus.HOT -> colors.warning
        ThermalStatus.CRITICAL -> colors.danger
    }

    TurboCard(
        modifier = modifier,
        borderColor = indicatorColor.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STATUS SUHU",
                style = androidx.compose.ui.text.TextStyle(
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )
            StatusChip(
                text = status.displayText.uppercase(),
                color = indicatorColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = temperatureC?.let { String.format("%.1f", it) } ?: "N/A",
                style = TelemetryValueStyle,
                color = colors.textPrimary
            )
            if (temperatureC != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "°C",
                    style = TelemetryUnitStyle,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun PerformanceGraph(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = TurboUroTheme.colors.accentPrimary,
    minY: Float = 0f,
    maxY: Float = 60f
) {
    val colors = TurboUroTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(colors.surfaceElevated, SmallButtonShape)
            .border(1.dp, colors.border, SmallButtonShape)
            .padding(8.dp)
    ) {
        if (dataPoints.size < 2) {
            Box(
                modifier = Modifier.fillMaxWidth().height(104.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mengumpulkan data telemetri...",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxWidth().height(104.dp)) {
                val width = size.width
                val height = size.height
                val range = (maxY - minY).coerceAtLeast(1f)
                val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

                val path = Path()
                dataPoints.forEachIndexed { index, value ->
                    val normalizedY = ((value - minY) / range).coerceIn(0f, 1f)
                    val x = index * stepX
                    val y = height - (normalizedY * height)
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = TurboUroTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(colors.surfaceElevated, CircleShape)
                    .border(1.dp, colors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            fontSize = 13.sp,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            SecondaryButton(
                text = actionButtonText,
                onClick = onActionClick
            )
        }
    }
}

@Composable
fun ErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    retryActionText: String? = null,
    onRetryClick: (() -> Unit)? = null
) {
    val colors = TurboUroTheme.colors

    TurboCard(
        modifier = modifier,
        borderColor = colors.danger.copy(alpha = 0.45f)
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = colors.danger
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = colors.textSecondary
        )
        if (retryActionText != null && onRetryClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            SecondaryButton(
                text = retryActionText,
                onClick = onRetryClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SettingsTile(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = TurboUroTheme.colors
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            role = Role.Button,
            onClick = onClick
        )
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .then(clickModifier)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }
        if (checked != null && onCheckedChange != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = colors.accentPrimary,
                    uncheckedThumbColor = colors.textMuted,
                    uncheckedTrackColor = colors.surfaceElevated
                )
            )
        }
    }
}
