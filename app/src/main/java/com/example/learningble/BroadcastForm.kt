package com.example.learningble.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.learningble.bluetooth.ChatServer
import com.example.learningble.currentBalance
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BroadcastForm() {
    var amount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val availableAmount = currentBalance

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
        onDispose { }
    }

    val view = LocalView.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            v.updatePadding(bottom = imeHeight)
            insets
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (amount.isEmpty()) "0₼" else "$amount₼",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Without commission",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Available $currentBalance₼",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = amount,
            onValueChange = { input ->
                val parsedValue = input.toDoubleOrNull()
                if (parsedValue == null || parsedValue <= availableAmount) {
                    amount = input
                }
            },
            singleLine = true,
            label = { Text("Enter the amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(1.dp)
                .alpha(0f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Description of payment") },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    ChatServer.broadcastMessage(amount, message)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = amount.isNotEmpty() && message.isNotEmpty()
        ) {
            Text("Transfer", fontSize = 16.sp)
        }
    }
}
