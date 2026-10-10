package com.mohamed.iosstylelauncher

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
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

    private var config by mutableStateOf(LauncherConfig())

    private lateinit var installedApps: List<LauncherApp>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        config = LauncherPreferences.load(this)
        installedApps = loadApps()

        setContent {
            MohamedCaliIOS(
                apps = installedApps,
                config = config
            )
        }
    }

    override fun onResume() {
        super.onResume()

        config = LauncherPreferences.load(this)
    }

    private fun loadApps(): List<LauncherApp> {
        val pm = packageManager

        return try {
            pm.getInstalledApplications(
                PackageManager.GET_META_DATA
            )
                .mapNotNull { app ->

                    if (app.packageName == packageName) {
                        return@mapNotNull null
                    }

                    val launchIntent =
                        pm.getLaunchIntentForPackage(
                            app.packageName
                        ) ?: return@mapNotNull null

                    try {
                        LauncherApp(
                            name = app.loadLabel(pm).toString(),
                            packageName = app.packageName,
                            icon = app.loadIcon(pm)
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                .distinctBy { it.packageName }
                .sortedBy {
                    it.name.lowercase(Locale.getDefault())
                }

        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun openApp(packageName: String) {
        try {
            val intent =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            if (intent != null) {
                startActivity(intent)
            }
        } catch (_: Exception) {
            // App-ku wuu maqnaan karaa ama lama furi karo.
        }
    }

    @Composable
    private fun MohamedCaliIOS(
        apps: List<LauncherApp>,
        config: LauncherConfig
    ) {
        var searchText by remember {
            mutableStateOf("")
        }

        var searchOpen by remember {
            mutableStateOf(false)
        }

        var showLibrary by remember {
            mutableStateOf(false)
        }

        var currentPage by remember {
            mutableIntStateOf(0)
        }

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

        val pageSize = config.columns * 3

        val pages = remember(filteredApps, pageSize) {
            filteredApps.chunked(pageSize.coerceAtLeast(1))
        }

        val pageCount = pages.size.coerceAtLeast(1)

        LaunchedEffect(pageCount) {
            currentPage = currentPage.coerceIn(
                0,
                pageCount - 1
            )
        }

        val currentApps = if (showLibrary) {
            filteredApps
        } else {
            pages.getOrNull(currentPage).orEmpty()
        }

        val dockApps = remember(apps) {
            val preferred = listOf(
                "phone",
                "message",
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

        val background = when (config.wallpaper) {
            "sunset" -> Brush.verticalGradient(
                listOf(
                    Color(0xFFFF9966),
                    Color(0xFFFF5E62),
                    Color(0xFF654EA3)
                )
            )

            "dark" -> Brush.verticalGradient(
                listOf(
                    Color(0xFF101116),
                    Color(0xFF252A34),
                    Color(0xFF11131A)
                )
            )

            "pink" -> Brush.verticalGradient(
                listOf(
                    Color(0xFFFFC3D8),
                    Color(0xFFD7B5FF),
                    Color(0xFF8BB8FF)
                )
            )

            else -> Brush.verticalGradient(
                listOf(
                    Color(0xFF55B8F5),
                    Color(0xFF819FF4),
                    Color(0xFFB5A5F3),
                    Color(0xFFE4B8E5)
                )
            )
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

        var totalDragX by remember {
            mutableFloatStateOf(0f)
        }

        var totalDragY by remember {
            mutableFloatStateOf(0f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .pointerInput(
                    showLibrary,
                    pageCount,
                    currentPage,
                    searchOpen
                ) {
                    detectDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()

                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        },
                        onDragEnd = {
                            val dx = totalDragX
                            val dy = totalDragY

                            if (kotlin.math.abs(dx) >
                                kotlin.math.abs(dy) &&
                                kotlin.math.abs(dx) > 70f
                            ) {
                                if (!showLibrary &&
                                    searchText.isBlank()
                                ) {
                                    if (dx < 0) {
                                        currentPage =
                                            (currentPage + 1)
                                                .coerceAtMost(
                                                    pageCount - 1
                                                )
                                    } else {
                                        currentPage =
                                            (currentPage - 1)
                                                .coerceAtLeast(0)
                                    }
                                }
                            } else if (
                                kotlin.math.abs(dy) > 90f
                            ) {
                                if (dy < 0) {
                                    showLibrary = true
                                    searchOpen = true
                                } else {
                                    if (showLibrary) {
                                        showLibrary = false
                                    } else {
                                        searchOpen = true
                                    }
                                }
                            }

                            totalDragX = 0f
                            totalDragY = 0f
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Qaybta sare: ma muujinayso magacaaga.

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showLibrary) {
                            "App Library"
                        } else {
                            "Home"
                        },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "●  ●  ●",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                if (config.showClock && !showLibrary) {
                    Text(
                        text = timeText,
                        color = Color.White,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                if (config.showDate && !showLibrary) {
                    Text(
                        text = dateText,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(18.dp))

                if (searchOpen || showLibrary) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Color.White.copy(alpha = 0.25f)
                            )
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⌕",
                            color = Color.White,
                            fontSize = 25.sp
                        )

                        Spacer(Modifier.width(10.dp))

                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            if (searchText.isEmpty()) {
                                Text(
                                    text = "Search Apps",
                                    color = Color.White.copy(
                                        alpha = 0.8f
                                    ),
                                    fontSize = 14.sp
                                )
                            }

                            BasicTextField(
                                value = searchText,
                                onValueChange = {
                                    searchText = it
                                    currentPage = 0
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 15.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Text(
                            text = "×",
                            color = Color.White,
                            fontSize = 22.sp,
                            modifier = Modifier.clickable {
                                searchText = ""
                                searchOpen = false
                                showLibrary = false
                            }
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Color.White.copy(alpha = 0.22f)
                            )
                            .clickable {
                                searchOpen = true
                            }
                            .padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⌕",
                            color = Color.White,
                            fontSize = 24.sp
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = "Search Apps",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                }

                Text(
                    text = if (showLibrary) {
                        "All Applications"
                    } else if (searchText.isNotBlank()) {
                        "Search Results"
                    } else {
                        " "
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(10.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(
                        config.columns.coerceIn(3, 5)
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 14.dp
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(20.dp)
                ) {
                    items(
                        items = currentApps,
                        key = { it.packageName }
                    ) { app ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    openApp(app.packageName)
                                },
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(config.iconSize.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            (config.iconSize * 0.27f).dp
                                        )
                                    )
                                    .background(
                                        Color.White.copy(
                                            alpha = 0.92f
                                        )
                                    )
                                    .padding(5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AndroidView(
                                    factory = { context ->
                                        ImageView(context).apply {
                                            scaleType =
                                                ImageView.ScaleType
                                                    .FIT_CENTER

                                            setImageDrawable(app.icon)
                                            contentDescription = app.name
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

                            if (config.showLabels) {
                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = app.name,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }

                if (!showLibrary &&
                    searchText.isBlank() &&
                    pageCount > 1
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.Center
                    ) {
                        repeat(pageCount) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(
                                        if (index == currentPage) {
                                            7.dp
                                        } else {
                                            5.dp
                                        }
                                    )
                                    .clip(
                                        RoundedCornerShape(50)
                                    )
                                    .background(
                                        Color.White.copy(
                                            alpha =
                                                if (index == currentPage) {
                                                    1f
                                                } else {
                                                    0.5f
                                                }
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Color.White.copy(
                                alpha = if (config.glassDock) {
                                    0.28f
                                } else {
                                    0.12f
                                }
                            )
                        )
                        .padding(
                            horizontal = 12.dp,
                            vertical = 12.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    dockApps.forEach { app ->
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(
                                    RoundedCornerShape(15.dp)
                                )
                                .background(
                                    Color.White.copy(alpha = 0.94f)
                                )
                                .clickable {
                                    openApp(app.packageName)
                                }
                                .padding(5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { context ->
                                    ImageView(context).apply {
                                        scaleType =
                                            ImageView.ScaleType
                                                .FIT_CENTER

                                        setImageDrawable(app.icon)
                                        contentDescription = app.name
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

                Spacer(Modifier.height(10.dp))

                Text(
                    text = if (showLibrary) {
                        "Swipe down or tap × to return"
                    } else {
                        "Swipe left or right to change pages"
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
