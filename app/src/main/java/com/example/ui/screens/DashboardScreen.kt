package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.SupabaseRecommendation

fun formatRecommendationTime(createdAt: String): String {
    if (createdAt.isBlank()) return "Live"
    return try {
        if (createdAt.contains("T")) {
            val timePart = createdAt.substringAfter("T").substringBefore("Z")
            val parts = timePart.split(":")
            if (parts.size >= 2) {
                "${parts[0]}:${parts[1]}"
            } else {
                timePart
            }
        } else if (createdAt.contains(" ")) {
            val timePart = createdAt.substringAfter(" ")
            val parts = timePart.split(":")
            if (parts.size >= 2) {
                "${parts[0]}:${parts[1]}"
            } else {
                timePart
            }
        } else {
            createdAt
        }
    } catch (e: Exception) {
        createdAt
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    recommendations: List<SupabaseRecommendation>,
    isLoading: Boolean,
    selectedId: Int?,
    onRefresh: () -> Unit,
    onSelect: (Int) -> Unit,
    onChatAbout: (SupabaseRecommendation) -> Unit
) {
    val sortedRecommendations: List<SupabaseRecommendation> = remember(recommendations, selectedId) {
        if (selectedId == null) recommendations
        else {
            val selected = recommendations.find { it.id == selectedId }
            val others = recommendations.filter { it.id != selectedId }
            if (selected != null) listOf(selected) + others else recommendations
        }
    }

    var selectedForReason by remember { mutableStateOf<SupabaseRecommendation?>(null) }

    if (selectedForReason != null) {
        AlertDialog(
            onDismissRequest = { selectedForReason = null },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(text = "${selectedForReason?.pair} Analysis", fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text(
                        text = selectedForReason?.content ?: "No analysis provided.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedForReason = null }) {
                    Text("Close")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                // Status Bar Simulation/Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "VX-Signals",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, shape = MaterialTheme.shapes.extraSmall)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Key Valid • 28 Days Left",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading && sortedRecommendations.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (sortedRecommendations.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp))
                    Text("No active calls found", modifier = Modifier.padding(top = 16.dp))
                    TextButton(onClick = onRefresh) { Text("Refresh") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                // Latest Signal / Hero
                item {
                    val first = sortedRecommendations.first()
                    HeroSignalCard(
                        call = first,
                        isSelected = first.id == selectedId,
                        onClick = { onSelect(first.id) },
                        onLongClick = { selectedForReason = first },
                        onChat = { onChatAbout(first) }
                    )
                }

                // Market Feed Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MARKET FEED",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Active Now",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Remaining Signals
                items(sortedRecommendations.drop(1), key = { it.id }) { call ->
                    SimpleCallItem(
                        call = call,
                        isSelected = call.id == selectedId,
                        onClick = { onSelect(call.id) },
                        onLongClick = { selectedForReason = call },
                        onChat = { onChatAbout(call) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroSignalCard(
    call: SupabaseRecommendation,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onChat: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF1E3A8A) else MaterialTheme.colorScheme.primary
    val backgroundBrush = if (isSelected) {
        Brush.linearGradient(colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
    } else {
        Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
    }

        val isBuy = call.title.contains("BUY", ignoreCase = true) || 
                    call.title.contains("LONG", ignoreCase = true) ||
                    call.title.contains("CALL", ignoreCase = true) ||
                    call.content.contains("BUY", ignoreCase = true) ||
                    ((call.targetPrice.toDoubleOrNull() ?: 0.0) > (call.entryPrice.toDoubleOrNull() ?: 0.0) && (call.entryPrice.toDoubleOrNull() ?: 0.0) > 0)

    val sideText = if (isBuy) "BUY / LONG" else "SELL / SHORT"
    val sideColor = if (isBuy) Color(0xFF4ADE80) else Color(0xFFF87171)

    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (isSelected) "SELECTED" else "HOT SIGNAL",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = sideColor,
                            shape = CircleShape
                        ) {
                            Text(
                                text = sideText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    val timeStr = formatRecommendationTime(call.createdAt)
                    val openLabel = "Opened $timeStr"
                    Text(
                        text = openLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = call.pair,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ENTRY PRICE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Text(text = call.entryPrice, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("GOAL (TP)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Text(text = call.targetPrice, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF4ADE80))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("STOP LOSS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Text(text = call.stopLoss, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFFF87171))
                            }
                        }
                    }
                    Button(
                        onClick = onChat,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("AI Chat", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleCallItem(
    call: SupabaseRecommendation,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onChat: () -> Unit
) {
    val surfaceColor = if (isSelected) Color(0xFF1E3A8A).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val borderColor = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f)

    val isBuySimple = call.title.contains("BUY", ignoreCase = true) || 
                    call.title.contains("LONG", ignoreCase = true) ||
                    call.title.contains("CALL", ignoreCase = true) ||
                    call.content.contains("BUY", ignoreCase = true) ||
                    ((call.targetPrice.toDoubleOrNull() ?: 0.0) > (call.entryPrice.toDoubleOrNull() ?: 0.0) && (call.entryPrice.toDoubleOrNull() ?: 0.0) > 0)

    val sideText = if (isBuySimple) "BUY" else "SELL"
    val sideColor = if (isBuySimple) Color(0xFF4ADE80) else Color(0xFFF87171)

    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = surfaceColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    color = sideColor.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = sideText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = sideColor
                            )
                            Icon(
                                imageVector = if (isBuySimple) Icons.Default.TrendingUp else Icons.Default.Warning, 
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = sideColor
                            )
                        }
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = call.pair,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val timeStr = formatRecommendationTime(call.createdAt)
                        val openLabel = "Opened $timeStr"
                        Text(
                            text = openLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Normal
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ENTRY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text(text = call.entryPrice, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GOAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text(text = call.targetPrice, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("S/L", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text(text = call.stopLoss, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFF87171))
                        }
                    }
                }
                
                TextButton(onClick = onChat) {
                    Text("AI Chat", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color.Unspecified) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
