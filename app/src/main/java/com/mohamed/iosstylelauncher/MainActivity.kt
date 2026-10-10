package com.mohamed.iosstylelauncher

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

data class LauncherApp(
    val name: String,
    val packageName: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                IOSStyleLauncher()
            }
        }
    }
}

@Composable
fun IOSStyleLauncher() {
    val context = LocalContext.current

    val apps = remember(context) {
        loadInstalledApps(context)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    val filteredApps = apps.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF2F4F8)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hello!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172033)
            )

            Text(
                text = "Your phone, your style.",
                fontSize = 15.sp,
                color = Color(0xFF697386)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⌕",
                    fontSize = 25.sp,
                    color = Color(0xFF687386)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFF172033)
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchText.isEmpty()) {
                                Text(
                                    text = "Search apps",
                                    color = Color(0xFF8A94A6),
                                    fontSize = 15.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (searchText.isBlank()) {
                    "Applications"
                } else {
                    "Search results"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF172033)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No launchable apps found.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(
                        items = filteredApps,
                        key = { it.packageName }
                    ) { app ->
                        AppIcon(
                            app = app,
                            onClick = {
                                launchApp(context, app.packageName)
                            }
                        )
                    }
                }
            }

            // iPhone-inspired bottom dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xDDE0E7F2))
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dockApps = listOf(
                    apps.firstOrNull {
                        it.name.contains("Phone", true) ||
                        it.name.contains("Call", true)
                    },
                    apps.firstOrNull {
                        it.name.contains("Message", true) ||
                        it.name.contains("SMS", true)
                    },
                    apps.firstOrNull {
                        it.name.contains("Camera", true)
                    },
                    apps.firstOrNull {
                        it.name.contains("Chrome", true) ||
                        it.name.contains("Browser", true)
                    }
                ).filterNotNull().distinctBy { it.packageName }

                if (dockApps.isEmpty()) {
                    Text(
                        text = "Apps",
                        color = Color(0xFF172033),
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    dockApps.forEach { app ->
                        AppIcon(
                            app = app,
                            onClick = {
                                launchApp(context, app.packageName)
                            },
                            showLabel = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun AppIcon(
    app: LauncherApp,
    onClick: () -> Unit,
    showLabel: Boolean = true
) {
    val context = LocalContext.current

    val iconBitmap = remember(app.packageName) {
        runCatching {
            context.packageManager
                .getApplicationIcon(app.packageName)
                .toBitmap(144, 144)
                .asImageBitmap()
        }.getOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = app.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(15.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFFDAE3F0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.name.take(1).uppercase(),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34445E)
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = app.name,
                fontSize = 11.sp,
                color = Color(0xFF253047),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun loadInstalledApps(context: Context): List<LauncherApp> {
    val packageManager = context.packageManager

    return try {
        packageManager
            .getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { info ->
                packageManager.getLaunchIntentForPackage(
                    info.packageName
                ) != null
            }
            .filter { info ->
                info.packageName != context.packageName
            }
            .map { info ->
                LauncherApp(
                    name = packageManager
                        .getApplicationLabel(info)
                        .toString(),
                    packageName = info.packageName
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }
            .toList()
    } catch (_: Exception) {
        emptyList()
    }
}

fun launchApp(
    context: Context,
    packageName: String
) {
    try {
        val intent = context.packageManager
            .getLaunchIntentForPackage(packageName)

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    } catch (_: Exception) {
        // The application may have been removed or disabled.
    }
}
