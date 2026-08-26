package com.pixelcountdown

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.pixelcountdown.data.CountdownItem
import com.pixelcountdown.data.CountdownRepository
import com.pixelcountdown.data.SettingsRepository
import com.pixelcountdown.receiver.NotificationHelper
import com.pixelcountdown.ui.about.AboutScreen
import com.pixelcountdown.ui.components.CountdownCard
import com.pixelcountdown.ui.components.CountdownEditDialog
import com.pixelcountdown.ui.components.SelectWidgetTimerDialog
import com.pixelcountdown.ui.settings.SettingsScreen
import com.pixelcountdown.ui.theme.PixelCountdownTheme
import com.pixelcountdown.widget.WidgetUpdateService
import kotlinx.coroutines.launch
import sh.calvin.reorderable.*

enum class AppScreen {
    Main, Settings, About
}

class MainActivity : ComponentActivity() {

    private val widgetIdState = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)
        handleIntent(intent)
        
        // Start widget update service
        val serviceIntent = Intent(this, WidgetUpdateService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        setContent {
            val settingsRepo = remember { SettingsRepository.getInstance(applicationContext) }
            PixelCountdownTheme(settingsRepository = settingsRepo) {
                AppNavigation(
                    targetWidgetId = widgetIdState.value,
                    onWidgetTimerSelected = { widgetId ->
                        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        setResult(Activity.RESULT_OK, resultValue)
                        widgetIdState.value = null
                        finish()
                    },
                    onDismissWidgetSelection = {
                        widgetIdState.value = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            widgetIdState.value = widgetId
        }
    }
}

@Composable
fun AppNavigation(
    targetWidgetId: Int?,
    onWidgetTimerSelected: (Int) -> Unit,
    onDismissWidgetSelection: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(AppScreen.Main) }

    BackHandler(enabled = currentScreen != AppScreen.Main || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentScreen = AppScreen.Main
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.HourglassEmpty, contentDescription = null) },
                    label = { Text("Timers") },
                    selected = currentScreen == AppScreen.Main,
                    onClick = {
                        currentScreen = AppScreen.Main
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_settings)) },
                    selected = currentScreen == AppScreen.Settings,
                    onClick = {
                        currentScreen = AppScreen.Settings
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_about)) },
                    selected = currentScreen == AppScreen.About,
                    onClick = {
                        currentScreen = AppScreen.About
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        when (currentScreen) {
            AppScreen.Main -> MainScreen(
                targetWidgetId = targetWidgetId,
                onWidgetTimerSelected = onWidgetTimerSelected,
                onDismissWidgetSelection = onDismissWidgetSelection,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            AppScreen.Settings -> SettingsScreen(onBack = { currentScreen = AppScreen.Main })
            AppScreen.About -> AboutScreen(onBack = { currentScreen = AppScreen.Main })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    targetWidgetId: Int?,
    onWidgetTimerSelected: (Int) -> Unit,
    onDismissWidgetSelection: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { CountdownRepository.getInstance(context) }
    val countdowns by repository.countdowns.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<CountdownItem?>(null) }
    var itemToDelete by remember { mutableStateOf<CountdownItem?>(null) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var activeWidgetBindingId by remember { mutableStateOf<Int?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Handle widget click / placement intent
    LaunchedEffect(targetWidgetId) {
        if (targetWidgetId != null && targetWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            activeWidgetBindingId = targetWidgetId
            if (countdowns.isEmpty()) {
                itemToEdit = null
                showEditDialog = true
            } else {
                showWidgetPicker = true
            }
        }
    }

    // Notification permission launcher for Android 13+ (API 33+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "PixelTimer - Countdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    showEditDialog = true
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new countdown"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (countdowns.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HourglassEmpty,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No countdowns yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button below to create your first countdown timer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val configuration = LocalConfiguration.current
                val isWideScreen = configuration.screenWidthDp >= 600

                if (isWideScreen) {
                    val lazyGridState = rememberLazyGridState()
                    val reorderableState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
                        val newList = countdowns.toMutableList().apply {
                            add(to.index, removeAt(from.index))
                        }
                        repository.reorderCountdowns(newList)
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = lazyGridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(countdowns, key = { it.id }) { item ->
                            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                                val elevation by animateFloatAsState(if (isDragging) 8f else 0f)

                                Box(
                                    modifier = Modifier
                                        .longPressDraggableHandle()
                                        .graphicsLayer {
                                            shadowElevation = elevation
                                            scaleX = if (isDragging) 1.05f else 1.0f
                                            scaleY = if (isDragging) 1.05f else 1.0f
                                            alpha = if (isDragging) 0.9f else 1f
                                        }
                                ) {
                                    CountdownCard(
                                        item = item,
                                        onEdit = {
                                            itemToEdit = item
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            itemToDelete = item
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val lazyListState = rememberLazyListState()
                    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                        val newList = countdowns.toMutableList().apply {
                            add(to.index, removeAt(from.index))
                        }
                        repository.reorderCountdowns(newList)
                    }

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(countdowns, key = { it.id }) { item ->
                            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                                val elevation by animateFloatAsState(if (isDragging) 8f else 0f)

                                Box(
                                    modifier = Modifier
                                        .longPressDraggableHandle()
                                        .graphicsLayer {
                                            shadowElevation = elevation
                                            scaleX = if (isDragging) 1.05f else 1.0f
                                            scaleY = if (isDragging) 1.05f else 1.0f
                                            alpha = if (isDragging) 0.9f else 1f
                                        }
                                ) {
                                    CountdownCard(
                                        item = item,
                                        onEdit = {
                                            itemToEdit = item
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            itemToDelete = item
                                        },
                                        modifier = Modifier.padding(bottom = if (item == countdowns.last()) 88.dp else 0.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Widget Selection Dialog
    if (showWidgetPicker && activeWidgetBindingId != null) {
        val currentBoundId = repository.getBoundCountdownId(activeWidgetBindingId!!)
        SelectWidgetTimerDialog(
            countdowns = countdowns,
            currentSelectedId = currentBoundId,
            onSelect = { selectedItem ->
                repository.bindWidgetToCountdown(activeWidgetBindingId!!, selectedItem.id)
                Toast.makeText(context, "Linked \"${selectedItem.title}\" to Home Widget", Toast.LENGTH_SHORT).show()
                showWidgetPicker = false
                val id = activeWidgetBindingId!!
                activeWidgetBindingId = null
                onWidgetTimerSelected(id)
            },
            onCreateNew = {
                showWidgetPicker = false
                itemToEdit = null
                showEditDialog = true
            },
            onDismiss = {
                showWidgetPicker = false
                activeWidgetBindingId = null
                onDismissWidgetSelection()
            }
        )
    }

    // Add / Edit Dialog
    if (showEditDialog) {
        CountdownEditDialog(
            initialItem = itemToEdit,
            onDismiss = {
                showEditDialog = false
                itemToEdit = null
                if (activeWidgetBindingId != null) {
                    val id = activeWidgetBindingId!!
                    activeWidgetBindingId = null
                    onDismissWidgetSelection()
                }
            },
            onSave = { title, targetEpochMillis ->
                val savedItem: CountdownItem
                if (itemToEdit != null) {
                    val updated = itemToEdit!!.copy(
                        title = title,
                        targetEpochMillis = targetEpochMillis
                    )
                    savedItem = repository.saveCountdown(updated)
                } else {
                    val newItem = CountdownItem(
                        title = title,
                        targetEpochMillis = targetEpochMillis
                    )
                    savedItem = repository.saveCountdown(newItem)
                }

                // If opened from a widget action, bind this new item to the widget automatically
                if (activeWidgetBindingId != null) {
                    repository.bindWidgetToCountdown(activeWidgetBindingId!!, savedItem.id)
                    Toast.makeText(context, "Linked \"${savedItem.title}\" to Home Widget", Toast.LENGTH_SHORT).show()
                    val id = activeWidgetBindingId!!
                    activeWidgetBindingId = null
                    onWidgetTimerSelected(id)
                }

                showEditDialog = false
                itemToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Delete Countdown",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${itemToDelete?.title}\"?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { repository.deleteCountdown(it.id) }
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
