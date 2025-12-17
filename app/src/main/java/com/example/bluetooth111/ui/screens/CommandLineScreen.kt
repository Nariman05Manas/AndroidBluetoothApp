package com.example.bluetooth111.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.bluetooth111.bluetooth.BluetoothManager

@Composable
fun CommandLineScreen(bluetoothManager: BluetoothManager) {
    var commandText by remember { mutableStateOf("") }
    val isConnected by bluetoothManager.isConnected.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusRequester.requestFocus()
            },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Статус подключения
        if (!isConnected) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "⚠️ Сначала подключитесь к устройству в разделе 'Поиск'",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Поле ввода команды
        OutlinedTextField(
            value = commandText,
            onValueChange = { commandText = it },
            label = { Text("Введите команду") },
            placeholder = { Text("Например: MOTOR:100 или SERVO:90") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        keyboardController?.show()
                    }
                },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (commandText.isNotBlank() && isConnected) {
                        bluetoothManager.sendCommand(commandText + "\n")
                        commandText = ""
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                }
            )
        )
        
        // Кнопка отправки
        Button(
            onClick = {
                if (commandText.isNotBlank()) {
                    bluetoothManager.sendCommand(commandText + "\n")
                    commandText = ""
                    keyboardController?.hide()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isConnected && commandText.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Отправить\n(Send command)")
        }
        
        // Кнопка скрыть клавиатуру
        TextButton(
            onClick = { keyboardController?.hide() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Скрыть клавиатуру\n(Hide keyboard)")
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

