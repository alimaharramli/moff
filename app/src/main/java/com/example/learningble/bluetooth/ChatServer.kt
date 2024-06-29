package com.example.learningble.bluetooth

import android.app.Application
import android.app.PendingIntent
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.learningble.MainActivity
import com.example.learningble.models.Message
import com.example.learningble.states.DeviceConnectionState
import com.example.learningble.utils.MESSAGE_UUID
import com.example.learningble.utils.SERVICE_UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val TAG = "ChatServerTAG"
private const val CHANNEL_ID = "ChatNotificationChannel"
private const val NOTIFICATION_ID = 1

object ChatServer {

    private var app: Application? = null
    private lateinit var bluetoothManager: BluetoothManager

    private var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var advertiseSettings: AdvertiseSettings = buildAdvertiseSettings()
    private var advertiseData: AdvertiseData = buildAdvertiseData()

    private val _messages = MutableLiveData<Message>()
    val messages = _messages as LiveData<Message>

    private var gattServer: BluetoothGattServer? = null
    private var gattServerCallback: BluetoothGattServerCallback? = null

    private val _deviceConnection = MutableLiveData<DeviceConnectionState>()
    val deviceConnection = _deviceConnection as LiveData<DeviceConnectionState>

    private var gatt: BluetoothGatt? = null
    private var messageCharacteristic: BluetoothGattCharacteristic? = null

    private var scanner: BluetoothLeScanner? = null
    private val scanResults = mutableMapOf<String, BluetoothDevice>()
    private var scanCallback: ScanCallback? = null

    private lateinit var scanFilters: List<ScanFilter>
    private lateinit var scanSettings: ScanSettings

    fun startServer(app: Application) {
        Log.d(TAG, "Starting server")
        this.app = app
        bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bluetoothManager.adapter
        setupGattServer(app)
        startAdvertisement()
        Log.d(TAG, "Server started")
    }

    fun stopServer() {
        Log.d(TAG, "Stopping server")
        stopAdvertising()
        stopScanning()
        Log.d(TAG, "Server stopped")
    }

    private fun setupGattServer(app: Application) {
        Log.i(TAG, "Setting up GATT server")
        gattServerCallback = GattServerCallback()

        gattServer = bluetoothManager.openGattServer(
            app,
            gattServerCallback
        ).apply {
            addService(setupGattService())
        }
        Log.i(TAG, "GATT server set up")
    }

    private fun setupGattService(): BluetoothGattService {
        Log.i(TAG, "Setting up GATT service")
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        messageCharacteristic = BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
        )
        service.addCharacteristic(messageCharacteristic)
        Log.i(TAG, "GATT service set up with characteristic $MESSAGE_UUID")
        return service
    }

    private fun startAdvertisement() {
        Log.d(TAG, "Starting advertisement")
        advertiser = adapter.bluetoothLeAdvertiser

        if (advertiseCallback == null) {
            advertiseCallback = DeviceAdvertiseCallback()

            advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        }
        Log.d(TAG, "Advertisement started")
    }

    private fun stopAdvertising() {
        Log.d(TAG, "Stopping advertisement")
        if (advertiseCallback != null)
            advertiser?.stopAdvertising(advertiseCallback)
        advertiseCallback = null
        Log.d(TAG, "Advertisement stopped")
    }

    private fun buildAdvertiseData(): AdvertiseData {
        Log.d(TAG, "Building advertise data")
        val dataBuilder = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .setIncludeDeviceName(true)

        return dataBuilder.build()
    }

    private fun buildAdvertiseSettings(): AdvertiseSettings {
        Log.d(TAG, "Building advertise settings")
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTimeout(0)
            .build()
    }

    private fun buildScanFilters(): List<ScanFilter> {
        val builder = ScanFilter.Builder()
        builder.setServiceUuid(ParcelUuid(SERVICE_UUID))
        val filter = builder.build()
        return listOf(filter)
    }


    private fun buildScanSettings(): ScanSettings {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
    }

    private fun startScanning() {
        Log.d(TAG, "Starting scan")
        scanFilters = buildScanFilters()
        scanSettings = buildScanSettings()
        scanner = adapter.bluetoothLeScanner
        scanResults.clear()
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                result.device?.let { device ->
                    scanResults[device.address] = device
                    Log.d(TAG, "Found device: ${device.name ?: "Unnamed Device"} - ${device.address}, RSSI: ${result.rssi}")
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                super.onBatchScanResults(results)
                results.forEach { result ->
                    result.device?.let { device ->
                        scanResults[device.address] = device
                        Log.d(TAG, "Found device: ${device.name ?: "Unnamed Device"} - ${device.address}, RSSI: ${result.rssi}")
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e(TAG, "Scan failed with error: $errorCode")
            }
        }
        scanner?.startScan(scanFilters, scanSettings, scanCallback)
    }



    private fun stopScanning() {
        Log.d(TAG, "Stopping scan")
        scanner?.stopScan(scanCallback)
        scanCallback = null
        Log.d(TAG, "Scan stopped. Found devices: ${scanResults.keys.joinToString(", ")}")
    }

    suspend fun broadcastMessage(amount: String, description: String) {
        val data = JSONObject().apply {
            put("receiver", JSONObject().apply {
                put("name", "Jane Smith")
                put("phone_number", "+0987654321")
                put("account_id", "receiver_account_456")
            })
            put("amount", amount.toDouble())
            put("currency", "USD")
            put("timestamp", System.currentTimeMillis())
            put("message", description)
            put("status", "pending")
        }

        val message = data.toString()
        Log.d(TAG, "Preparing to broadcast message: $message")

        withContext(Dispatchers.IO) {
            startScanning()
            delay(3000) // Wait for 10 seconds to gather scan results
            stopScanning()

            Log.d(TAG, "Starting broadcast to found devices one by one")
            scanResults.values.forEach { device ->
//                connectToDeviceAndSendMessage(device, message)
            }
        }
    }

    private fun connectToDeviceAndSendMessage(device: BluetoothDevice, message: String) {
        val gattClientCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                } else {
                    gatt.close()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(SERVICE_UUID)
                    val characteristic = service?.getCharacteristic(MESSAGE_UUID)
                    if (characteristic != null) {
                        characteristic.value = message.toByteArray(Charsets.UTF_8)
                        val success = gatt.writeCharacteristic(characteristic)
                        Log.d(TAG, "Writing message to device: ${device.name ?: "Unnamed Device"} - ${device.address}, success: $success")
                    } else {
                        Log.e(TAG, "Characteristic not found on device: ${device.name ?: "Unnamed Device"} - ${device.address}")
                    }
                }
                gatt.close()
            }
        }
        device.connectGatt(app, false, gattClientCallback)
    }



    private class GattServerCallback : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(TAG, "onConnectionStateChange: isSuccess=$isSuccess, isConnected=$isConnected")
            if (isSuccess && isConnected) {
                _deviceConnection.postValue(DeviceConnectionState.Connected(device))
                Log.d(TAG, "Device connected: ${device.address}")
            } else {
                _deviceConnection.postValue(DeviceConnectionState.Disconnected)
                Log.d(TAG, "Device disconnected or connection failed: ${device.address}")
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            Log.d(TAG, "onCharacteristicWriteRequest: device=${device.address}, requestId=$requestId, characteristic=${characteristic.uuid}")
            if (characteristic.uuid == MESSAGE_UUID) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                Log.d(TAG, "Received message: $message")
                message?.let {
                    _messages.postValue(Message.RemoteMessage(it))
                    app?.let { appContext ->
                        Log.d(TAG, "Calling showNotification with message: $it")
                        showNotification(appContext, it)
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, message: String) {
        Log.d(TAG, "Preparing to show notification for message: $message")

        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        Log.d(TAG, "NotificationManagerCompat created")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        Log.d(TAG, "PendingIntent created")

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New Message")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        Log.d(TAG, "NotificationCompat.Builder created")

        with(notificationManager) {
            notify(NOTIFICATION_ID, builder.build())
        }
        Log.d(TAG, "Notification shown")

        Handler(Looper.getMainLooper()).postDelayed({
            with(notificationManager) {
                cancel(NOTIFICATION_ID)
            }
            Log.d(TAG, "Notification cancelled after 30 seconds")
        }, 30000)
    }

    private class DeviceAdvertiseCallback : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            val errorMessage = "Advertise failed with error: $errorCode"
            Log.d(TAG, errorMessage)
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertisement started successfully with settings: $settingsInEffect")
        }
    }
}
