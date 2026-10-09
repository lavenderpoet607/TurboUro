package com.turbouro.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.turbouro.app.manager.AppLaunchManager
import com.turbouro.app.manager.GameItem
import com.turbouro.app.manager.GameScanner
import com.turbouro.app.storage.GameProfileStore
import com.turbouro.app.ui.component.CustomTurboIconButton
import com.turbouro.app.ui.component.EmptyState
import com.turbouro.app.ui.component.PrimaryButton
import com.turbouro.app.ui.component.StatusChip
import com.turbouro.app.ui.component.TurboCard
import com.turbouro.app.ui.theme.CardShape
import com.turbouro.app.ui.theme.SmallButtonShape
import com.turbouro.app.ui.theme.TurboUroTheme
import kotlinx.coroutines.launch

@Composable
fun GameLibraryScreen(
    scanner: GameScanner,
    profileStore: GameProfileStore,
    launchManager: AppLaunchManager,
    onNavigateToDetail: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = TurboUroTheme.colors
    val scope = rememberCoroutineScope()

    var gamesList by remember { mutableStateOf<List<GameItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var allInstalledApps by remember { mutableStateOf<List<GameItem>>(emptyList()) }
    var dialogSearchQuery by remember { mutableStateOf("") }

    val reloadGames = {
        scope.launch {
            isLoading = true
            gamesList = scanner.scanGames()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        reloadGames()
    }

    val filteredGames = gamesList.filter {
        val matchesQuery = it.name.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            1 -> it.isAutoDetectedGame
            2 -> it.isManual
            else -> true
        }
        matchesQuery && matchesFilter
    }

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
                    text = "Pustaka Game",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "${gamesList.size} game terdaftar",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }

            CustomTurboIconButton(
                icon = Icons.Default.Add,
                contentDescription = "Tambah Game",
                onClick = {
                    scope.launch {
                        allInstalledApps = scanner.getAllInstalledApps()
                        showAddDialog = true
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari game...", color = colors.textMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.textSecondary
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = SmallButtonShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedBorderColor = colors.accentPrimary,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("Semua", "Otomatis", "Manual")
            filters.forEachIndexed { index, name ->
                val isSelected = selectedFilter == index
                Box(
                    modifier = Modifier
                        .clip(SmallButtonShape)
                        .background(if (isSelected) colors.accentPrimary.copy(alpha = 0.2f) else colors.surfaceElevated)
                        .border(1.dp, if (isSelected) colors.accentPrimary else colors.border, SmallButtonShape)
                        .clickable { selectedFilter = index }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) colors.accentPrimary else colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.accentPrimary)
            }
        } else if (filteredGames.isEmpty()) {
            EmptyState(
                title = "Belum Ada Game Ditemukan",
                description = "TurboUro dapat memindai game otomatis atau Anda dapat menambahkan aplikasi yang terpasang secara manual.",
                icon = Icons.Default.SportsEsports,
                actionButtonText = "Tambah Game Manual",
                onActionClick = {
                    scope.launch {
                        allInstalledApps = scanner.getAllInstalledApps()
                        showAddDialog = true
                    }
                }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredGames, key = { it.packageName }) { game ->
                    val profile = remember(game.packageName) {
                        profileStore.getProfile(game.packageName, game.name)
                    }

                    TurboCard(
                        onClick = { onNavigateToDetail(game.packageName) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (game.icon != null) {
                                val bitmap = remember(game.packageName) {
                                    try {
                                        game.icon.toBitmap(96, 96).asImageBitmap()
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = game.name,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(colors.surfaceElevated, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsEsports,
                                            contentDescription = null,
                                            tint = colors.accentPrimary
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(colors.surfaceElevated, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = null,
                                        tint = colors.accentPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = game.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = game.packageName,
                                    fontSize = 11.sp,
                                    color = colors.textMuted,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Profil: ${profile.profileType.displayName}",
                                    fontSize = 12.sp,
                                    color = colors.accentSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(SmallButtonShape)
                                    .background(colors.accentPrimary)
                                    .clickable {
                                        val launched = launchManager.launchGame(game.packageName, game.name)
                                        if (launched) {
                                            onShowMessage("Menjalankan ${game.name} dengan profil ${profile.profileType.displayName}")
                                        } else {
                                            onShowMessage("Gagal membuka game ${game.name}")
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Main",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        val filteredApps = allInstalledApps.filter {
            it.name.contains(dialogSearchQuery, ignoreCase = true) ||
                    it.packageName.contains(dialogSearchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Tambah Game Manual",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(modifier = Modifier.height(360.dp)) {
                    OutlinedTextField(
                        value = dialogSearchQuery,
                        onValueChange = { dialogSearchQuery = it },
                        placeholder = { Text("Cari aplikasi...", color = colors.textMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = SmallButtonShape
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scanner.addManualGame(app.packageName)
                                        showAddDialog = false
                                        onShowMessage("${app.name} ditambahkan ke pustaka game")
                                        reloadGames()
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = app.packageName,
                                        fontSize = 11.sp,
                                        color = colors.textMuted
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(colors.accentPrimary.copy(alpha = 0.2f), SmallButtonShape)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Pilih",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accentPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Tutup", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = CardShape
        )
    }
}
