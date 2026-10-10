package com.mohamed.iosstylelauncher

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class LauncherApp(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val installedApps = loadApps()

        setContent {
            MohamedCaliIOS(installedApps)
        }
    }

    private fun loadApps(): List<LauncherApp> {
        val pm = packageManager

        return try {
            pm.getInstalledApplications(
                PackageManager.GET_META_DATA
            )
                .mapNotNull { app: ApplicationInfo ->
                    val packageName = app.packageName

                    if (packageName == this.packageName) {
                        return@mapNotNull null
                    }

                    val launchIntent =
                        pm.getLaunchIntentForPackage(packageName)
                            ?: return@mapNotNull null

                    try {
                        LauncherApp(
                            name = app.loadLabel(pm).toString(),
                            packageName = packageName,
                            icon = app.loadIcon(pm)
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                .distinctBy { it.packageName }
                .sortedBy { it.name.lowercase(Locale.getDefault()) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun openApp(packageName: String) {
        try {
            val intent =
                packageManager.getLaunchIntentForPackage(packageName)

            if (intent != null) {
                startActivity(intent)
            }
        } catch (_: Exception) {
            // App may no longer be installed.
        }
    }

    @Composable
    private fun MohamedCaliIOS(
        apps: List<LauncherApp>
    ) {
        var searchText by remember { mutableStateOf("") }

        var currentTime by remember {
            mutableStateOf(LocalDateTime.now())
        }

        LaunchedEffect(Unit) {
            while (true) {
                currentTime = LocalDateTime.now()
                delay(1000)
            }
        }

        val filteredApps = remember(apps, searchText) {
            apps.filter {
                it.name.contains(
                    searchText,
                    ignoreCase = true
                )
            }
        }

        val dockApps = remember(apps) {
            val preferred = listOf(
                "phone",
                "messages",
                "camera",
                "chrome"
            )

            val selected = preferred.mapNotNull { keyword ->
                apps.firstOrNull {
                    it.name.contains(
                        keyword,
                        ignoreCase = true
                    )
                }
            }.distinctBy { it.packageName }

            (selected + apps.filter {
                it !in selected
            }).take(4)
        }

        val timeText = currentTime.format(
            DateTimeFormatter.ofPattern(
                "h:mm",
                Locale.getDefault()
            )
        )

        val dateText = currentTime.format(
            DateTimeFormatter.ofPattern(
                "EEEE, MMMM d",
                Locale.ENGLISH
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF79C7F5),
                            Color(0xFF9CCAFB),
                            Color(0xFFB6B9F8),
                            Color(0xFFE5B8E8)
                        )
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {

                Spacer(modifier = Modifier.height(12.dp))

                // Personal identity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MAXAMED CALI",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "iOS",
                            fontSize = 12.sp,
                            color = Color.White.copy(
                                alpha = 0.85f
                            )
                        )
                    }

                    Text(
                        text = "●  ●  ●",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))

                // Clock
                Text(
                    text = timeText,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )

                Text(
                    text = dateText,
                    fontSize = 17.sp,
                    color = Color.White.copy(alpha = 0.95f)
                )

                Spacer(modifier = Modifier.height(25.dp))

                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            Color.White.copy(alpha = 0.35f)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⌕",
                        fontSize = 26.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (searchText.isEmpty()) {
                            Text(
                                text = "Search Apps",
                                color = Color.White.copy(
                                    alpha = 0.9f
                                ),
                                fontSize = 15.sp
                            )
                        }

                        BasicTextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = if (searchText.isBlank()) {
                        "Your Apps"
                    } else {
                        "Search Results"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // App grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        bottom = 12.dp
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(22.dp)
                ) {
                    items(
                        items = filteredApps,
                        key = { it.packageName }
                    ) { app ->

                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                openApp(app.packageName)
                            }
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(
                                        RoundedCornerShape(17.dp)
                                    )
                                    .background(
                                        Color.White.copy(
                                            alpha = 0.92f
                                        )
                                    )
                                    .padding(5.dp),
                                contentAlignment =
                                    Alignment.Center
                            ) {
                                AndroidView(
                                    factory = { context ->
                                        ImageView(context).apply {
                                            scaleType =
                                                ImageView.ScaleType
                                                    .FIT_CENTER
                                            setImageDrawable(app.icon)
                                            contentDescription =
                                                app.name
                                        }
                                    },
                                    update = { imageView ->
                                        imageView.setImageDrawable(
                                            app.icon
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = app.name,
                                fontSize = 10.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }

                // Dock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Color.White.copy(alpha = 0.30f)
                        )
                        .padding(
                            horizontal = 14.dp,
                            vertical = 12.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dockApps.forEach { app ->

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(
                                    RoundedCornerShape(16.dp)
                                )
                                .background(
                                    Color.White.copy(
                                        alpha = 0.95f
                                    )
                                )
                                .clickable {
                                    openApp(app.packageName)
                                }
                                .padding(5.dp),
                            contentAlignment =
                                Alignment.Center
                        ) {
                            AndroidView(
                                factory = { context ->
                                    ImageView(context).apply {
                                        scaleType =
                                            ImageView.ScaleType
                                                .FIT_CENTER
                                        setImageDrawable(app.icon)
                                        contentDescription =
                                            app.name
                                    }
                                },
                                update = { imageView ->
                                    imageView.setImageDrawable(
                                        app.icon
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Home page indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(
                                    if (index == 0) 7.dp else 5.dp
                                )
                                .clip(
                                    RoundedCornerShape(50)
                                )
                                .background(
                                    Color.White.copy(
                                        alpha = if (index == 0) {
                                            1f
                                        } else {
                                            0.55f
                                        }
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
