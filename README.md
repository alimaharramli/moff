# moff

Offline mobile payments over Bluetooth Low Energy. Two phones, no internet.

A hackathon prototype built in a day. Handles the communication layer for peer-to-peer transactions using BLE GATT services. One phone broadcasts as a BLE peripheral, the other connects as a central device, and transaction data is exchanged over a custom GATT characteristic.

**Not a production payment system.** This is the communication part only. No encryption, no ledger, no fraud detection.

## How it works

1. Seller opens the app and starts advertising a BLE service
2. Buyer scans for nearby devices and connects
3. Transaction details are serialized as JSON and sent over BLE
4. Both sides get a confirmation notification

Built with Kotlin, Jetpack Compose, and the Android BLE API.

## Stack

- Kotlin + Jetpack Compose
- Android BLE (BluetoothGattServer / BluetoothGatt)
- Custom GATT service with negotiated MTU for larger payloads
- High-priority notifications for transaction confirmations
