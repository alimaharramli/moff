package com.example.learningble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningble.bluetooth.ChatServer
import com.example.learningble.presentation.BroadcastForm
import com.example.learningble.ui.theme.LearningBLETheme
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

private const val TAG = "MainActivityTAG"
public const val PHONE_NUMBER = "+994556098874"
class MainActivity : ComponentActivity() {

//    override fun onStop() {
//        super.onStop()
//        ChatServer.stopServer()
//    }

    @Composable
    fun CustomTopAppBar() {
        Column {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .border(BorderStroke(1.dp, Color.Gray), CircleShape)
                    ) {
                        Text(
                            text = "MA",
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                    }
                },
                backgroundColor = Color.White,
                contentColor = Color.Black,
                elevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Divider(color = Color.Gray, thickness = 1.dp)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LearningBLETheme {
                val result = remember { mutableStateOf<Int?>(100) }
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    result.value = it.resultCode
                }

                LaunchedEffect(key1 = true){
                    Dexter.withContext(this@MainActivity)
                        .withPermissions(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                launcher.launch(intent)
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: List<PermissionRequest?>?,
                                token: PermissionToken?
                            ) {

                            }
                        })
                        .check()
                }

//                LaunchedEffect(key1 = result.value){
//                    if(result.value == RESULT_OK){
                        ChatServer.startServer(application)
//                    }
//                }

                Scaffold(topBar = {
                    CustomTopAppBar()
                }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        color = MaterialTheme.colors.background
                    ) {
                        BroadcastForm()
                    }
                }
            }
        }
    }

}
