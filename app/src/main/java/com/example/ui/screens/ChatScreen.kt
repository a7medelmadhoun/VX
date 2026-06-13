package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.SupabaseRecommendation

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    selectedRecommendation: SupabaseRecommendation?,
    onSendMessage: (String) -> Unit,
    isSending: Boolean,
    onBack: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val greetingText = if (selectedRecommendation != null) {
        "Chatting about ${selectedRecommendation.pair}"
    } else {
        "Ask me anything about the crypto market"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = { 
                    CenterAlignedTopAppBarTitle(selectedRecommendation)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.Transparent,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type your message...", color = Color.White.copy(alpha = 0.3f)) },
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                0.5.dp, 
                                Color.White.copy(alpha = 0.1f), 
                                RoundedCornerShape(24.dp)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        maxLines = 5
                    )
                    Spacer(Modifier.width(12.dp))
                    FloatingActionButton(
                        onClick = {
                            if (textInput.isNotBlank() && !isSending) {
                                onSendMessage(textInput)
                                textInput = ""
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (textInput.isNotBlank() && !isSending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(52.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = greetingText,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "I'm your VX AI assistant. Ask me about entry points, market trends, or current signals.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val sortedMessages = remember(messages) {
                    messages.sortedBy { it.timestamp }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sortedMessages) { message ->
                        ChatBubble(message)
                    }
                    if (isSending) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }
    }
}

fun formatChatTime(timestamp: Long): String {
    return try {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}

sealed interface ChatBlock {
    data class TextBlock(val text: String) : ChatBlock
    data class TableBlock(val headers: List<String>, val rows: List<List<String>>) : ChatBlock
}

fun parseChatContent(text: String): List<ChatBlock> {
    val lines = text.split("\n")
    val blocks = mutableListOf<ChatBlock>()
    val currentTableLines = mutableListOf<String>()
    
    val flushTable = {
        if (currentTableLines.isNotEmpty()) {
            val table = parseMarkdownTable(currentTableLines)
            if (table != null) {
                blocks.add(table)
            } else {
                blocks.add(ChatBlock.TextBlock(currentTableLines.joinToString("\n")))
            }
            currentTableLines.clear()
        }
    }
    
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.contains("|")) {
            currentTableLines.add(line)
        } else {
            flushTable()
            if (trimmed.isNotEmpty()) {
                blocks.add(ChatBlock.TextBlock(line))
            }
        }
    }
    flushTable()
    
    return blocks
}

fun parseMarkdownTable(lines: List<String>): ChatBlock.TableBlock? {
    val parsedRows = lines.map { line ->
        line.split("|")
            .map { it.trim() }
            .filterIndexed { index, _ -> 
                !(index == 0 && line.startsWith("|") && line.trim().startsWith("|")) &&
                !(index == line.split("|").lastIndex && line.endsWith("|"))
            }
    }.filter { row ->
        row.any { cell -> cell.isNotBlank() && !cell.all { it == '-' || it == ':' || it == ' ' } }
    }
    
    if (parsedRows.isEmpty()) return null
    
    val headers = parsedRows.first()
    val dataRows = if (parsedRows.size > 1) parsedRows.subList(1, parsedRows.size) else emptyList()
    
    return ChatBlock.TableBlock(headers, dataRows)
}

@Composable
fun ChatTable(headers: List<String>, rows: List<List<String>>) {
    val columnCount = headers.size
    if (columnCount == 0) return
    
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .horizontalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier.widthIn(min = (columnCount * 110).dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        maxLines = 2
                    )
                }
            }
            
            // Data Rows
            rows.forEachIndexed { rowIndex, row ->
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                val bg = if (rowIndex % 2 == 1) Color.White.copy(alpha = 0.04f) else Color.Transparent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until columnCount) {
                        val cellText = if (i < row.size) row[i] else ""
                        Text(
                            text = cellText,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f)),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val containerColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        val blocks = remember(message.text) {
            if (message.isUser) {
                listOf(ChatBlock.TextBlock(message.text))
            } else {
                parseChatContent(message.text)
            }
        }
        
        val containsTable = remember(blocks) {
            blocks.any { it is ChatBlock.TableBlock }
        }

        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = shape,
            tonalElevation = if (message.isUser) 4.dp else 1.dp,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = if (containsTable) 380.dp else 310.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                blocks.forEach { block ->
                    when (block) {
                        is ChatBlock.TextBlock -> {
                            val textStr = block.text.trim()
                            if (textStr.isNotEmpty()) {
                                val isHeader = textStr.startsWith("#")
                                Text(
                                    text = textStr.removePrefix("#").trim(),
                                    style = if (isHeader) {
                                        MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold, 
                                            color = Color.White
                                        )
                                    } else {
                                        MaterialTheme.typography.bodyLarge.copy(
                                            lineHeight = 22.sp,
                                            color = Color.White.copy(alpha = 0.95f),
                                            fontSize = 15.sp,
                                            letterSpacing = 0.3.sp
                                        )
                                    }
                                )
                            }
                        }
                        is ChatBlock.TableBlock -> {
                            ChatTable(headers = block.headers, rows = block.rows)
                        }
                    }
                }
            }
        }
        val timeStr = formatChatTime(message.timestamp)
        if (timeStr.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            modifier = Modifier.padding(end = 64.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "AI is analyzing...",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun CenterAlignedTopAppBarTitle(selectedRecommendation: SupabaseRecommendation?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "VX AI CHAT",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            letterSpacing = 1.sp
        )
        if (selectedRecommendation != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = selectedRecommendation.pair,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
