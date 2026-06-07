package com.mypocketnews.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var modelExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            keyboardController?.hide()
            snackbarHostState.showSnackbar("Settings saved")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("LLM Model", style = MaterialTheme.typography.titleMedium)

            val selectedLabel = com.mypocketnews.ui.settings.OpenRouterModels
                .find { it.id == uiState.modelId }?.label ?: OpenRouterModels.first().label

            ExposedDropdownMenuBox(
                expanded = modelExpanded,
                onExpandedChange = { modelExpanded = !modelExpanded }
            ) {
                OutlinedTextField(
                    value = selectedLabel,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = null
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = modelExpanded,
                    onDismissRequest = { modelExpanded = false }
                ) {
                    com.mypocketnews.ui.settings.OpenRouterModels.forEach { modelOption ->
                        DropdownMenuItem(
                            text = { Text(modelOption.label) },
                            onClick = {
                                viewModel.onModelChange(modelOption.id)
                                modelExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = { viewModel.onApiKeyChange(it) },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            TextButton(
                onClick = {
                    viewModel.save(uiState.apiKey, uiState.modelId)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
