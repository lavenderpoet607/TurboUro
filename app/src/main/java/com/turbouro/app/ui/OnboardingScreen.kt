package com.turbouro.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbouro.app.ui.component.PrimaryButton
import com.turbouro.app.ui.component.SecondaryButton
import com.turbouro.app.ui.theme.TurboUroTheme

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val colors = TurboUroTheme.colors
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPageData(
            title = "Telemetri Game Nyata",
            description = "Pantau FPS, frame time, temperatur baterai, dan status perangkat secara langsung tanpa data palsu.",
            icon = Icons.Default.Analytics
        ),
        OnboardingPageData(
            title = "Profil Game Mandiri",
            description = "Simpan konfigurasi performa, mode game, dan preferensi overlay terpisah untuk setiap judul game favorit.",
            icon = Icons.Default.SportsEsports
        ),
        OnboardingPageData(
            title = "Integritas Sistem & Performa Aman",
            description = "TurboUro membaca kemampuan hardware secara akurat dan tidak memalsukan indikator booster atau statistik fiktif.",
            icon = Icons.Default.Security
        )
    )

    val currentData = pages[currentPage]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (currentPage < pages.size - 1) {
                SecondaryButton(
                    text = "Lewati",
                    onClick = onComplete
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(colors.surfaceElevated, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = currentData.icon,
                    contentDescription = null,
                    tint = colors.accentPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = currentData.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currentData.description,
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                pages.forEachIndexed { index, _ ->
                    val isSelected = index == currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .background(
                                color = if (isSelected) colors.accentPrimary else colors.border,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            if (currentPage < pages.size - 1) {
                PrimaryButton(
                    text = "Lanjut",
                    onClick = { currentPage++ }
                )
            } else {
                PrimaryButton(
                    text = "Mulai Sekarang",
                    onClick = onComplete
                )
            }
        }
    }
}
