package com.mohamed.iosstylelauncher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                LauncherSettingsScreen(
                    onOpenHome = {
                        val intent = Intent(
                            Intent.ACTION_MAIN
                        ).addCategory(
                            Intent.CATEGORY_HOME
                        )

                        startActivity(intent)
                    },
                    onChooseDefaultHome = {
                        try {
                            startActivity(
                                Intent(
                                    Settings.ACTION_HOME_SETTINGS
                                )
                            )
                        } catch (_: Exception) {
                            startActivity(
                                Intent(
                                    Settings.ACTION_SETTINGS
                                )
                            )
                        }
                    },
                    onUninstall = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        ).apply {
                            data = android.net.Uri.parse(
                                "package:$packageName"
                            )
                        }

                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun LauncherSettingsScreen(
    onOpenHome: () -> Unit,
    onChooseDefaultHome: () -> Unit,
    onUninstall: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var config by remember {
        mutableStateOf(
            LauncherPreferences.load(context)
        )
    }

    fun updateConfig(newConfig: LauncherConfig) {
        config = newConfig
        LauncherPreferences.save(context, newConfig)
    }

    val background = Brush.verticalGradient(
        listOf(
            Color(0xFF101A32),
            Color(0xFF344C79),
            Color(0xFF8B9ED1)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Maxamed Cali iOS",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Text(
            text = "Customization Center",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Color.White.copy(alpha = 0.12f),
                    RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "WALLPAPER",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ocean" to "Ocean",
                            "sunset" to "Sunset",
                            "dark" to "Dark",
                            "pink" to "Pink"
                        ).forEach { (value, title) ->
                            FilterChip(
                                selected = config.wallpaper == value,
                                onClick = {
                                    updateConfig(
                                        config.copy(wallpaper = value)
                                    )
                                },
                                label = { Text(title) }
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.25f)
                    )

                    Text(
                        "HOME SCREEN",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Apps per row: ${config.columns}",
                        color = Color.White
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(4, 5).forEach { count ->
                            FilterChip(
                                selected = config.columns == count,
                                onClick = {
                                    updateConfig(
                                        config.copy(columns = count)
                                    )
                                },
                                label = { Text("$count columns") }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Icon size: ${config.iconSize} dp",
                        color = Color.White
                    )

                    Slider(
                        value = config.iconSize.toFloat(),
                        onValueChange = {
                            updateConfig(
                                config.copy(iconSize = it.toInt())
                            )
                        },
                        valueRange = 48f..72f
                    )
                }

                item {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.25f)
                    )

                    Text(
                        "DISPLAY OPTIONS",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    SettingSwitch(
                        title = "Show clock",
                        checked = config.showClock,
                        onCheckedChange = {
                            updateConfig(
                                config.copy(showClock = it)
                            )
                        }
                    )

                    SettingSwitch(
                        title = "Show date",
                        checked = config.showDate,
                        onCheckedChange = {
                            updateConfig(
                                config.copy(showDate = it)
                            )
                        }
                    )

                    SettingSwitch(
                        title = "Show app names",
                        checked = config.showLabels,
                        onCheckedChange = {
                            updateConfig(
                                config.copy(showLabels = it)
                            )
                        }
                    )

                    SettingSwitch(
                        title = "Liquid Glass dock",
                        checked = config.glassDock,
                        onCheckedChange = {
                            updateConfig(
                                config.copy(glassDock = it)
                            )
                        }
                    )
                }

                item {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.25f)
                    )

                    Text(
                        "LAUNCHER",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Button(
                        onClick = onChooseDefaultHome,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set as Default Home")
                    }

                    OutlinedButton(
                        onClick = onOpenHome,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Home Screen")
                    }

                    OutlinedButton(
                        onClick = onUninstall,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("App Information / Uninstall")
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Settings are saved automatically.",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.White
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
