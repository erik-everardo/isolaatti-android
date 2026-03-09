package com.isolaatti.settings.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.isolaatti.R
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.settings.presentation.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@Serializable
object SettingsMainRoute

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            IsolaattiTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = SettingsMainRoute) {
                    composable<SettingsMainRoute> {
                        SettingsScreen(
                            onNavigateBack = { finish() },
                            onNavigateToAccountSettings = { /* Implementar más adelante */ }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAccountSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showFeedSquads by remember { mutableStateOf(true) }
    var showFeedSuggested by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Sección de Cuenta
            SettingsSectionTitle(title = "Cuenta")
            ListItem(
                headlineContent = { Text("Ajustes de cuenta") },
                modifier = Modifier.clickable { onNavigateToAccountSettings() }
            )
            ListItem(
                headlineContent = { Text("Cerrar sesión") },
                modifier = Modifier.clickable { 
                    viewModel.logout()
                    onNavigateBack()
                }
            )

            HorizontalDivider()

            // Sección de Feed
            SettingsSectionTitle(title = "Contenido del feed")
            ListItem(
                headlineContent = { Text("Ver contenido de mis squads") },
                trailingContent = {
                    Switch(checked = showFeedSquads, onCheckedChange = { showFeedSquads = it })
                }
            )
            ListItem(
                headlineContent = { Text("Ver contenido sugerido") },
                trailingContent = {
                    Switch(checked = showFeedSuggested, onCheckedChange = { showFeedSuggested = it })
                }
            )

            HorizontalDivider()

            // Sección Peligrosa
            SettingsSectionTitle(title = "Acciones avanzadas")
            ListItem(
                headlineContent = { Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { /* Implementar más adelante */ }
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
