package com.example.learningble

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningble.bluetooth.ChatServer
import com.example.learningble.bluetooth.ChatServer.broadcastMessage
import com.example.learningble.bluetooth.ChatServer.connectToDeviceAndSendMessage
import com.example.learningble.bluetooth.ChatServer.connectedDevices
import com.example.learningble.dto.TransactionDto
import com.example.learningble.models.TransactionMessage
import com.example.learningble.ui.theme.LearningBLETheme
import com.google.gson.Gson


class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val message = intent.getStringExtra("message")
        Log.d("NOTIFICATION_MESSAGE", message.toString())
        val msgObj = Gson().fromJson(message, TransactionMessage::class.java)

        val deviceAddress = intent.getStringExtra("device_address")

        // Use the deviceAddress to respond to the device later
        if (deviceAddress != null) {
            val device = ChatServer.connectedDevices[deviceAddress]
            if (device != null) {
                Log.d("STORED_DEVICE",device.toString())
                // You can now interact with the device
                val currentDevice = connectedDevices[device.address] as BluetoothDevice
                val transaction = TransactionDto(msgObj.amount, msgObj.timestamp, msgObj.receiver.phoneNumber, PHONE_NUMBER, 1)
                connectToDeviceAndSendMessage(currentDevice, Gson().toJson(transaction))
            }
        }


        setContent {
            LearningBLETheme {
                NotificationScreen(message = msgObj)
            }
        }
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

    @Composable
    fun NotificationScreen(message: TransactionMessage) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Request",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Request", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Full Name")
                Text(text = message.receiver.name, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Amount")
                Text(text = "${message.amount} ${message.currency}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Description")
                Text(text = message.message, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Handle payment */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Pay ${message.amount} ${message.currency}")
                    Text(text = "Without Commission", color = Color.Gray)
                }
            }
        }
    }