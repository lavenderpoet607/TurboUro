package com.turbouro.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbouro.app.model.GameSession
import com.turbouro.app.storage.SessionHistoryStore
import com.turbouro.app.ui.component.CustomTurboIconButton
import com.turbouro.app.ui.component.EmptyState
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.CardShape
import com.turbouro.app.ui.theme.TurboUroTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionHistoryScreen(
    sessionStore: SessionHistoryStore,
    onNavigateToDetail: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = TurboUroTheme.colors
    var sessions by remember { mutableStateOf<List<GameSession>>(emptyList()) }
    var showClearDialog by remember { mutableStateOf(false) }

    val reload = {
        sessions = sessionStore.getAllSessions()
    }

    LaunchedEffect(Unit) {
        reload()
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Permainan",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "${sessions.size} sesi tercatat",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }

            if (sessions.isNotEmpty()) {
                CustomTurboIconButton(
                    icon = Icons.Default.DeleteSweep,
                    contentDescription = "Hapus Semua Riwayat",
                    onClick = { showClearDialog = true },
                    tint = colors.danger
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            EmptyState(
                title = "Belum Ada Sesi Permainan",
                description = "Statistik sesi permainan yang diselesaikan melalui TurboUro akan otomatis tersimpan di sini.",
                icon = Icons.Default.History
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    TurboCard(
                        onClick = { onNavigateToDetail(session.id) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.gameName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${session.formattedDuration} (${dateFormatter.format(Date(session.startTime))})",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (session.averageFps > 0) String.format("Rata-rata %.1f FPS", session.averageFps) else "FPS N/A",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accentPrimary
                            )

                            if (session.maxTemperature > 0) {
                                Text(
                                    text = String.format("Maks %.1f°C", session.maxTemperature),
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
                                )
                            }

                            Text(
                                text = "Baterai ${session.batteryStart}% -> ${session.batteryEnd}%",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Hapus Semua Riwayat?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Seluruh data riwayat sesi permainan yang tersimpan akan dihapus permanen.",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    sessionStore.clearAllSessions()
                    showClearDialog = false
                    reload()
                    onShowMessage("Semua riwayat sesi berhasil dihapus")
                }) {
                    Text("Hapus", color = colors.danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = CardShape
        )
    }
}
