package com.pixelcountdown.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pixelcountdown.R
import com.pixelcountdown.data.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository.getInstance(context) }
    val currentTheme by repository.theme.collectAsState()
    val currentFont by repository.fontFamily.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThemeSelector(
                selectedTheme = currentTheme,
                onThemeSelected = { repository.setTheme(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(R.string.settings_font),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FontSelector(
                selectedFont = currentFont,
                onFontSelected = { repository.setFontFamily(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_notifications)) },
                supportingContent = { Text(stringResource(R.string.settings_notifications_desc)) },
                leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                modifier = Modifier.clickable {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun ThemeSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val options = listOf(
        "system" to stringResource(R.string.theme_system),
        "light" to stringResource(R.string.theme_light),
        "dark" to stringResource(R.string.theme_dark)
    )

    Column(Modifier.selectableGroup()) {
        options.forEach { (key, label) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (key == selectedTheme),
                        onClick = { onThemeSelected(key) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (key == selectedTheme),
                    onClick = null
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun FontSelector(
    selectedFont: String,
    onFontSelected: (String) -> Unit
) {
    val options = listOf(
        "default" to stringResource(R.string.font_default),
        "serif" to stringResource(R.string.font_serif),
        "monospace" to stringResource(R.string.font_monospace)
    )

    Column(Modifier.selectableGroup()) {
        options.forEach { (key, label) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (key == selectedFont),
                        onClick = { onFontSelected(key) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (key == selectedFont),
                    onClick = null
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
