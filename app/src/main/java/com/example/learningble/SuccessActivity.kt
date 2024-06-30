package com.example.learningble

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SuccessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val amount = intent.getStringExtra("amount") ?: ("-" + currentAmount)
        val currency = intent.getStringExtra("currency") ?: "₼"
        val status = intent.getStringExtra("status") ?: "In Progress"
        val cardInfo = intent.getStringExtra("cardInfo") ?: "From m10 Balance (offline mode)"
        val balance = intent.getStringExtra("balance") ?: "Current balance: 10.00₼"

        setContent {
            if (amount != null) {
                SuccessScreen(
                    amount = amount,
                    currency = currency,
                    status = status,
                    cardInfo = cardInfo,
                    balance = balance,
                    onBackClick = { val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SuccessScreen(
    amount: String,
    currency: String,
    status: String,
    cardInfo: String,
    balance: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for the checkmark icon
        // Image(
        //     painter = painterResource(id = R.drawable.ic_checkmark), // Replace with your checkmark icon resource
        //     contentDescription = null,
        //     modifier = Modifier.size(100.dp)
        // )

        Spacer(modifier = Modifier.height(24.dp))

        // Displaying the amount and status
        Text(
            text = "$amount $currency",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = status,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card information placeholder
        Text(
            text = cardInfo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image(
            //     painter = painterResource(id = R.drawable.ic_mastercard), // Replace with your card icon resource
            //     contentDescription = null,
            //     modifier = Modifier.size(24.dp)
            // )
            Text(
                text = balance,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Back")
        }
    }
}
