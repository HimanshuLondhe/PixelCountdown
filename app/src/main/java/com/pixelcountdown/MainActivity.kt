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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pixelcountdown.data.CountdownItem
import com.pixelcountdown.data.CountdownRepository
import com.pixelcountdown.receiver.NotificationHelper
import com.pixelcountdown.ui.components.CountdownCard
import com.pixelcountdown.ui.components.CountdownEditDialog
import com.pixelcountdown.ui.components.SelectWidgetTimerDialog
import com.pixelcountdown.ui.theme.PixelCountdownTheme
import com.pixelcountdown.widget.PixelCountdownWidgetProvider

class MainActivity : ComponentActivity() {

    private val widgetIdState = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)
        handleIntent(intent)

        setContent {
            PixelCountdownTheme {
                MainScreen(
                    targetWidgetId = widgetIdState.value,
                    onWidgetTimerSelected = { widgetId ->
                        // Finish configure activity if opened from widget setup
                        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        setResult(Activity.RESULT_OK, resultValue)
                        widgetIdState.value = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    targetWidgetId: Int?,
    onWidgetTimerSelected: (Int) -> Unit,
    onDismissWidgetSelection: () -> Unit
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
                // If none exist, prompt to create one immediately
                itemToEdit = null
                showEditDialog = true
            } else {
                // Give option to choose from available timers
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
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Pixel Countdown",
                        fontWeight = FontWeight.Bold
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = countdowns,
                        key = { it.id }
                    ) { item ->
                        CountdownCard(
                            item = item,
                            onEdit = {
                                itemToEdit = item
                                showEditDialog = true
                            },
                            onDelete = {
                                itemToDelete = item
                            },
                            onPinToggle = {
                                repository.setPinned(item.id)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
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
