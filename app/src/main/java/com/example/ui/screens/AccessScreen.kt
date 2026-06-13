package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccessScreen(
    onKeyValidated: (String) -> Unit,
    isValidating: Boolean,
    errorMessage: String?,
    promoCode: String,
    currentPrice: Double,
    onApplyPromo: (String) -> Unit,
    isRetrievingKey: Boolean,
    keyAlertMessage: String?,
    onRetrieveLatestKey: (String, (String) -> Unit) -> Unit,
    purchasedKeysList: List<String> = emptyList(),
    isFetchingPurchasedKeys: Boolean = false,
    onRetrieveAllKeys: (String) -> Unit = {},
    isGeneratingPaymentUrl: Boolean = false,
    onPurchaseKey: (String, (String) -> Unit) -> Unit = { _, _ -> }
) {
    var keyInput by remember { mutableStateOf("") }
    var promoInput by remember { mutableStateOf("") }
    var paymentEmailInput by remember { mutableStateOf("") }
    var listKeysEmailInput by remember { mutableStateOf("") }
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val tabTitles = listOf("Activation", "Crypto Pay", "My Keys")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP BAR: Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VX-Signals",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            // Screen Title
            Text(
                text = "VIP PREMIUM PORTAL",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )

            // Dynamic TabRow with elegant sliding and styling
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    )
                }
            }

            // Scrollable Content depending on current active tab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // TAB 1: KEY ACTIVATION
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ACTIVATION DECK",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "Enter your purchased Premium VX Key below to unlock real-time intelligence feeds, VIP technical analysis, and AI chat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            OutlinedTextField(
                                value = keyInput,
                                onValueChange = { keyInput = it },
                                label = { Text("VX Access Key (VX-XXXX)") },
                                placeholder = { Text("VX-...") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                singleLine = true,
                                isError = errorMessage != null,
                                shape = MaterialTheme.shapes.large,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = { onKeyValidated(keyInput.trim()) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                                    .height(56.dp),
                                enabled = keyInput.isNotBlank() && !isValidating,
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            ) {
                                if (isValidating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "UNLOCK PORTAL",
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 2: CUSTOM PAYMENT INTERFACE
                        val portalDesc = if (promoCode.isNotEmpty()) {
                            "Get professional signals with AI verification. $${String.format("%.2f", currentPrice)} / month (Code: $promoCode applied!). Pay with Crypto via Binance, Bybit, Phantom or Wallet."
                        } else {
                            "Get professional signals with AI verification. $25/month. Pay with Crypto via Binance, Bybit, Phantom or Wallet."
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.extraLarge,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "SECURE CRYPTO ECOSYSTEM",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = portalDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (promoCode.isNotEmpty()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                                )

                                OutlinedTextField(
                                    value = promoInput,
                                    onValueChange = { promoInput = it },
                                    placeholder = { Text("Enter Promo Code") },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    singleLine = true,
                                    trailingIcon = {
                                        TextButton(onClick = { onApplyPromo(promoInput) }) {
                                            Text("APPLY")
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                    )
                                )

                                Spacer(Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = paymentEmailInput,
                                    onValueChange = { paymentEmailInput = it },
                                    label = { Text("Payment Email Address") },
                                    placeholder = { Text("example@domain.com") },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                    )
                                )

                                Spacer(Modifier.height(16.dp))
                                
                                Button(
                                    onClick = {
                                        val trimmedEmail = paymentEmailInput.trim()
                                        if (trimmedEmail.isEmpty()) {
                                            Toast.makeText(
                                                context,
                                                "Please enter your payment email first to link your purchase correctly!", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            onPurchaseKey(trimmedEmail) { url ->
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                    enabled = !isGeneratingPaymentUrl
                                ) {
                                    if (isGeneratingPaymentUrl) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = "PAY SECURELY ($currentPrice USD)",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        onRetrieveLatestKey(paymentEmailInput) { retrievedKey ->
                                            keyInput = retrievedKey
                                            selectedTabIndex = 0
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    enabled = !isRetrievingKey && paymentEmailInput.isNotBlank()
                                ) {
                                    if (isRetrievingKey) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = "Auto-Activate After Payment",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (keyAlertMessage != null) {
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = keyAlertMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (keyAlertMessage.contains("✅")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 3: MY PURCHASED KEYS BOX
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MY PURCHASED KEYS BOX",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "Enter your payment email address to search our ecosystem and list all keys generated for your transactions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = listKeysEmailInput,
                                onValueChange = { listKeysEmailInput = it },
                                label = { Text("Payment Email") },
                                placeholder = { Text("example@domain.com") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = MaterialTheme.shapes.medium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                )
                            )

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = { onRetrieveAllKeys(listKeysEmailInput.trim()) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                enabled = listKeysEmailInput.isNotBlank() && !isFetchingPurchasedKeys,
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            ) {
                                if (isFetchingPurchasedKeys) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "FETCH MY VIP KEYS",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            if (purchasedKeysList.isNotEmpty()) {
                                Text(
                                    text = "FOUND VIP KEYS (${purchasedKeysList.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .padding(bottom = 12.dp)
                                )

                                purchasedKeysList.forEach { key ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = key,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Tap Right to Copy / Left to Use",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Row {
                                                // Autofill & use button
                                                IconButton(
                                                    onClick = {
                                                        keyInput = key
                                                        selectedTabIndex = 0
                                                        Toast.makeText(
                                                            context,
                                                            "Key selected! Tap UNLOCK PORTAL to activate.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LockOpen,
                                                        contentDescription = "Use Key",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                Spacer(Modifier.width(8.dp))

                                                // Copy button
                                                IconButton(
                                                    onClick = {
                                                        val annotatedKey = androidx.compose.ui.text.buildAnnotatedString { append(key) }
                                                        clipboardManager.setText(annotatedKey)
                                                        Toast.makeText(
                                                            context,
                                                            "Key copied successfully! ✅",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy key",
                                                        tint = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (listKeysEmailInput.isNotBlank() && !isFetchingPurchasedKeys) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                    shape = MaterialTheme.shapes.medium,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "No keys found yet. If you paid just now, please return to 'Crypto Pay' and click 'Auto-Activate After Payment' to generate your key.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
