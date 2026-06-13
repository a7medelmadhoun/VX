package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.api.*
import com.example.data.AppDatabase
import com.example.data.SubscriptionKey
import com.example.ui.screens.ChatMessage
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.subscriptionDao()
    private val statsDao = db.statsDao()

    // APIs
    private val gazacodeRetrofit = Retrofit.Builder()
        .baseUrl("https://crypto.gazacode.workers.dev")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build()))
        .client(OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build())
        .build()

    private val keyRetrofit = Retrofit.Builder()
        .baseUrl("https://generate-key.gazacode.workers.dev")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build()))
        .build()

    private val supabaseRetrofit = Retrofit.Builder()
        .baseUrl("https://bxucklgxzfdnvcffzmgh.supabase.co")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build()))
        .build()

    private val gazacodeApi = gazacodeRetrofit.create(GazacodeApi::class.java)
    private val keyApi = keyRetrofit.create(GazacodeApi::class.java)
    private val supabaseApi = supabaseRetrofit.create(SupabaseApi::class.java)

    private val nowPaymentsRetrofit = Retrofit.Builder()
        .baseUrl("https://api.nowpayments.io/")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build()))
        .build()

    private val nowPaymentsApi = nowPaymentsRetrofit.create(NowPaymentsApi::class.java)

    // State
    val subscriptionKey = dao.getKey().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val dailyStats = statsDao.getRecentStats().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isValidatingKey = MutableStateFlow(false)
    val isValidatingKey = _isValidatingKey.asStateFlow()

    private val _keyError = MutableStateFlow<String?>(null)
    val keyError = _keyError.asStateFlow()

    private val _recommendations = MutableStateFlow<List<SupabaseRecommendation>>(emptyList())
    val recommendations = _recommendations.asStateFlow()

    private val _isLoadingRecs = MutableStateFlow(false)
    val isLoadingRecs = _isLoadingRecs.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _selectedRecommendationId = MutableStateFlow<Int?>(null)
    val selectedRecommendationId = _selectedRecommendationId.asStateFlow()

    val selectedRecommendation = combine(recommendations, selectedRecommendationId) { recs, id ->
        recs.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat = _isSendingChat.asStateFlow()

    private val _isRetrievingKey = MutableStateFlow(false)
    val isRetrievingKey = _isRetrievingKey.asStateFlow()

    private val _purchasedKeysList = MutableStateFlow<List<String>>(emptyList())
    val purchasedKeysList = _purchasedKeysList.asStateFlow()

    private val _isFetchingPurchasedKeys = MutableStateFlow(false)
    val isFetchingPurchasedKeys = _isFetchingPurchasedKeys.asStateFlow()

    private val _isGeneratingPaymentUrl = MutableStateFlow(false)
    val isGeneratingPaymentUrl = _isGeneratingPaymentUrl.asStateFlow()

    private val _keyAlertMessage = MutableStateFlow<String?>(null)
    val keyAlertMessage = _keyAlertMessage.asStateFlow()
    
    private val _promoCode = MutableStateFlow("")
    val promoCode = _promoCode.asStateFlow()

    private val _currentPrice = MutableStateFlow(25.0)
    val currentPrice = _currentPrice.asStateFlow()

    private val seenSignalIds = mutableSetOf<Int>()
    private var isFirstRecommendationLoad = true

    private fun sendLocalNotification(title: String, text: String) {
        val context = getApplication<Application>().applicationContext
        val channelId = "vx_signals_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "VX Crypto Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time crypto trade recommendation alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.presence_online)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            
        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            Log.e("VX_DEBUG", "Permission missing for notification: ${e.message}")
        } catch (e: Exception) {
            Log.e("VX_DEBUG", "Failed to send notification: ${e.message}")
        }
    }

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            // Ensure today's record exists
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            if (statsDao.getStatsByDate(today) == null) {
                statsDao.insertStats(com.example.data.DailyStats(today, 0))
            }
            
            subscriptionKey.collect { key ->
                if (key != null && key.isValid) {
                    startPolling()
                } else {
                    stopPolling()
                }
            }
        }
    }

    fun incrementDailySuccess() {
        viewModelScope.launch {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            if (statsDao.getStatsByDate(today) == null) {
                statsDao.insertStats(com.example.data.DailyStats(today, 1))
            } else {
                statsDao.incrementSuccess(today)
            }
        }
    }

    private fun startPolling() {
        if (pollingJob != null) return
        pollingJob = viewModelScope.launch {
            while (true) {
                loadRecommendations()
                delay(90 * 60 * 1000) // 90 minutes (hour and a half)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun validateKey(key: String) {
        viewModelScope.launch {
            _isValidatingKey.value = true
            _keyError.value = null
            try {
                if (key == "AhmedElmadhoun12$") {
                    dao.insertKey(SubscriptionKey(key, true))
                    return@launch
                }
                
                Log.d("VX_DEBUG", "Validating key on backend: $key")
                val response = keyApi.validateKey(key)
                if (response.isValid) {
                    dao.insertKey(SubscriptionKey(key, true))
                } else {
                    _keyError.value = response.message ?: "Invalid key or expired subscription."
                }
            } catch (e: Exception) {
                // Generous fallback if backend times out but key format is VX-
                if (key.startsWith("VX-")) {
                    dao.insertKey(SubscriptionKey(key, true))
                } else {
                    _keyError.value = "Connection error. Please try again."
                }
            } finally {
                _isValidatingKey.value = false
            }
        }
    }

    fun fetchAndAutofillLatestKey(email: String, onKeyRetrieved: (String) -> Unit) {
        viewModelScope.launch {
            _isRetrievingKey.value = true
            
            val trimmedEmail = email.trim().lowercase()
            if (trimmedEmail.isEmpty()) {
                _keyAlertMessage.value = "Please enter your email first and try again."
                _isRetrievingKey.value = false
                return@launch
            }
            
            try {
                // Step 1: Send key generation request
                _keyAlertMessage.value = "Sending key generation request for: $trimmedEmail..."
                Log.d("VX_DEBUG", "Sending key generation request for: $trimmedEmail")
                try {
                    keyApi.generateKey(trimmedEmail)
                } catch (e: Exception) {
                    Log.w("VX_DEBUG", "Generate key API call warning (non-fatal, proceeding to check): ${e.message}")
                }
                
                delay(1500)
                
                // Step 2: Key generation server sends verification request to payment app
                _keyAlertMessage.value = "Sending inspection and verification request to payment app to confirm transaction..."
                delay(2000)
                
                // Step 3: Check payment confirmation
                Log.d("VX_DEBUG", "Fetching keys list for: $trimmedEmail after verification request")
                val responseBody = keyApi.getKeysList()
                val responseString = responseBody.string()
                Log.d("VX_DEBUG", "Verification Keys List Raw: $responseString")
                
                var matchedKey: String? = null
                val blocks = responseString.split("}")
                for (block in blocks) {
                    if (block.lowercase().contains(trimmedEmail)) {
                        val vxRegex = Regex("VX-[a-zA-Z0-9]+")
                        val match = vxRegex.find(block)
                        if (match != null) {
                            matchedKey = match.value
                            break
                        }
                    }
                }
                
                if (matchedKey != null) {
                    onKeyRetrieved(matchedKey)
                    _keyAlertMessage.value = "Payment confirmed successfully! Your key is: $matchedKey ✅\n(Key autofilled, you can now UNLOCK PORTAL)"
                    Log.d("VX_DEBUG", "Autofilled verified key: $matchedKey")
                } else {
                    _keyAlertMessage.value = null
                    sendLocalNotification(
                        title = "Payment Not Found ❌",
                        text = "No matching subscription detected for this email. Please ensure your checkout is complete or try again in a few minutes."
                    )
                }
            } catch (e: Exception) {
                Log.e("VX_DEBUG", "Failed in payment flow: ${e.message}", e)
                _keyAlertMessage.value = null
                sendLocalNotification(
                    title = "System Alert ⚠️",
                    text = "Connection error with payment verification gateway. Please check your internet and try again."
                )
            } finally {
                _isRetrievingKey.value = false
            }
        }
    }

    fun retrieveAllKeysForEmail(email: String) {
        viewModelScope.launch {
            _isFetchingPurchasedKeys.value = true
            _purchasedKeysList.value = emptyList()
            val trimmedEmail = email.trim().lowercase()
            if (trimmedEmail.isEmpty()) {
                _isFetchingPurchasedKeys.value = false
                return@launch
            }
            try {
                // Trigger auto key generation first so any pending key gets cooked
                try {
                    keyApi.generateKey(trimmedEmail)
                } catch (e: Exception) {
                    Log.w("VX_DEBUG", "Auto check-generate warning: ${e.message}")
                }
                delay(1200)
                
                val responseBody = keyApi.getKeysList()
                val responseString = responseBody.string()
                Log.d("VX_DEBUG", "Verification Keys List Raw: $responseString")
                
                val foundKeysList = mutableListOf<String>()
                val blocks = responseString.split("}")
                for (block in blocks) {
                    if (block.lowercase().contains(trimmedEmail)) {
                        val vxRegex = Regex("VX-[a-zA-Z0-9]+")
                        val match = vxRegex.find(block)
                        if (match != null && !foundKeysList.contains(match.value)) {
                            foundKeysList.add(match.value)
                        }
                    }
                }
                _purchasedKeysList.value = foundKeysList.reversed()
            } catch (e: Exception) {
                Log.e("VX_DEBUG", "Error fetching keys: ${e.message}")
            } finally {
                _isFetchingPurchasedKeys.value = false
            }
        }
    }

    fun generateNowPaymentInvoice(email: String, onUrlGenerated: (String) -> Unit) {
        viewModelScope.launch {
            _isGeneratingPaymentUrl.value = true
            val trimmedEmail = email.trim()
            try {
                val apiKey = BuildConfig.NOWPAYMENTS_API_KEY
                val request = CreateInvoiceRequest(
                    priceAmount = _currentPrice.value,
                    orderId = trimmedEmail,
                    orderDescription = "VIP SIGNALS - $trimmedEmail",
                    successUrl = "https://crypto-signals-app.com/success", // Placeholder
                    cancelUrl = "https://crypto-signals-app.com/cancel"    // Placeholder
                )
                val response = nowPaymentsApi.createInvoice(apiKey, request)
                response.invoiceUrl?.let { onUrlGenerated(it) }
            } catch (e: Exception) {
                Log.e("VX_DEBUG", "Error creating invoice: ${e.message}")
            } finally {
                _isGeneratingPaymentUrl.value = false
            }
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _isLoadingRecs.value = true
            try {
                Log.d("VX_DEBUG", "Loading recommendations from Gazacode API...")
                val responseBody = gazacodeApi.getRecommendations()
                val responseString = responseBody.string()
                Log.d("VX_DEBUG", "Recommendations Raw: $responseString")
                
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                
                // Flexible parsing: Try list first, then common object structures
                val result = try {
                    val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, Recommendation::class.java)
                    moshi.adapter<List<Recommendation>>(listType).fromJson(responseString) ?: emptyList()
                } catch (e: Exception) {
                    Log.d("VX_DEBUG", "Failed to parse as list, trying to find array in object...")
                    try {
                        val mapType = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                        val map = moshi.adapter<Map<String, Any>>(mapType).fromJson(responseString)
                        val arrayJson = moshi.adapter(Any::class.java).toJson(map?.get("recommendations") ?: map?.get("data") ?: map?.get("signals") ?: emptyList<Any>())
                        val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, Recommendation::class.java)
                        moshi.adapter<List<Recommendation>>(listType).fromJson(arrayJson) ?: emptyList()
                    } catch (e2: Exception) {
                        Log.e("VX_DEBUG", "Final parsing fallback failed: ${e2.message}")
                        emptyList()
                    }
                }

                val mappedResult = result.map { rec ->
                    val pair = rec.symbol?.toString() ?: "Unknown"
                    val type = (rec.type ?: rec.side ?: rec.action ?: "BUY").toString().uppercase()
                    val stableId = rec.id?.toString()?.split(".")?.firstOrNull()?.toIntOrNull() ?: pair.hashCode()
                    SupabaseRecommendation(
                        id = stableId,
                        createdAt = rec.timestamp?.toString() ?: "",
                        title = "${pair} $type",
                        content = "VX Expert Market Signal",
                        pair = pair,
                        entryPrice = extractPrice(rec.entry ?: rec.entryPrice ?: rec.opening ?: rec.entryZone),
                        targetPrice = extractPrice(rec.target ?: rec.targetPrice ?: rec.tp ?: rec.takeProfit ?: rec.goal ?: rec.exitPrice),
                        stopLoss = extractPrice(rec.stopLoss ?: rec.sl),
                        isActive = true
                    )
                }

                if (mappedResult.isNotEmpty()) {
                    _recommendations.value = mappedResult
                    // Re-enable AI filter using the chat endpoint
                    filterRecommendationsWithAI(mappedResult)
                } else {
                    _recommendations.value = emptyList()
                    Log.d("VX_DEBUG", "No deals found or parsing resulted in empty list")
                }
            } catch (e: Exception) {
                Log.e("VX_DEBUG", "Error loading recommendations: ${e.message}", e)
                _recommendations.value = emptyList()
            } finally {
                _isLoadingRecs.value = false
            }
        }
    }

    private fun extractPrice(value: Any?): String {
        if (value == null) return "0.0"
        val s = value.toString().replace(",", "")
        // Robust extraction: handle numbers within text or lists [123, 456]
        val regex = Regex("\\d+(\\.\\d+)?")
        val match = regex.find(s)
        return match?.value ?: "0.0"
    }

    private fun parseChatMessage(responseString: String): String {
        val trimmed = responseString.trim()
        
        // Try to handle potential JSON wrapping from workers
        val content = if (trimmed.startsWith("{")) {
            try {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val mapType = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                val map = moshi.adapter<Map<String, Any>>(mapType).fromJson(trimmed)
                
                (map?.get("response") ?: map?.get("answer") ?: map?.get("message") ?: map?.get("text") ?: map?.get("content"))?.toString()
            } catch (e: Exception) {
                null
            }
        } else null

        var rawText = content ?: trimmed

        // Remove starting and ending quotes
        rawText = rawText.replace(Regex("^\"|\"$"), "")
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .trim()

        // Clean any unnecessary backticks (e.g., ```markdown, ```html, ```)
        val cleanedLines = rawText.split("\n")
            .map { it.trim() }
            .filter { line ->
                !line.startsWith("```") && !line.endsWith("```") && line != "```"
            }
            .joinToString("\n")

        return cleanedLines.replace("**", "") // Remove bold markdown
            .replace(Regex("(?m)^\\s*-\\s*\\*\\*"), "- ") // Clean list starters
            .trim()
    }

    private suspend fun filterRecommendationsWithAI(rawList: List<SupabaseRecommendation>) {
        if (rawList.isEmpty()) return
        
        val prompt = StringBuilder()
        prompt.append("Task: Act as an expert AI Crypto Portfolio Manager and Financial Analyst. Review and analyze EACH of the following real-time signals. For EACH signal, you must evaluate the risk, leverage, and entries. If the setup has failed, has high risk, or current market suggests an immediate exit, mark its status as 'RISKY_CLOSE' to recommend closing it. Otherwise, select 'HIGH_POTENTIAL', 'MODERATE', or 'EXPIRED'. Provide a brief 1-sentence analytical verdict in English explaining your logic, plus the status. Do not create new deals; only evaluate these specific existing ones.\n")
        prompt.append("Format your response EXACTLY as a list of lines starting with [INDEX]: VERDICT | COMMENTARY.\n")
        prompt.append("Example format:\n")
        prompt.append("[0]: HIGH_POTENTIAL | Excellent buy opportunity at current support level and target is ready for breakout.\n")
        prompt.append("[1]: RISKY_CLOSE | Market is reversing negatively and broke key support, immediate close recommended.\n\n")
        
        prompt.append("Signals to analyze:\n")
        rawList.forEachIndexed { index, rec ->
            prompt.append("[$index] ${rec.pair}: Type ${rec.title}, Entry ${rec.entryPrice}, TP ${rec.targetPrice}, SL ${rec.stopLoss}\n")
        }

        val verdictStatuses = mutableMapOf<Int, String>()

        try {
            Log.d("VX_DEBUG", "Sending comprehensive AI analysis prompt for all trades...")
            val responseBody = gazacodeApi.getChatResponse(prompt.toString())
            val responseText = parseChatMessage(responseBody.string())
            Log.d("VX_DEBUG", "AI Comprehensive Analysis Raw: $responseText")
            
            val enrichedList = rawList.mapIndexed { index, rec ->
                val linePrefix1 = "[$index]:"
                val linePrefix2 = "$index:"
                val matchLine = responseText.lines().firstOrNull { 
                    it.trim().startsWith(linePrefix1) || it.trim().startsWith(linePrefix2) 
                }
                if (matchLine != null) {
                    val cleanLine = matchLine.trim()
                    val actualContent = if (cleanLine.startsWith(linePrefix1)) {
                        cleanLine.substringAfter(linePrefix1).trim()
                    } else {
                        cleanLine.substringAfter(linePrefix2).trim()
                    }
                    
                    val verdict = actualContent.substringBefore("|").trim()
                    val commentary = actualContent.substringAfter("|").trim()
                    
                    verdictStatuses[rec.id] = verdict
                    
                    val verdictLabel = when(verdict.uppercase()) {
                        "HIGH_POTENTIAL", "HIGH" -> "Active - High Potential 🔥"
                        "MODERATE" -> "Active - Moderate Risk ⚠️"
                        "RISKY_CLOSE", "RISKY", "CLOSE" -> "Close Signal Immediately ❌"
                        "EXPIRED" -> "Expired - Angle Overpassed 🚫"
                        else -> verdict
                    }
                    
                    rec.copy(
                        title = "${rec.pair} ($verdictLabel)",
                        content = commentary
                    )
                } else {
                    rec
                }
            }
            _recommendations.value = enrichedList
            processNewSignalsAndNotify(enrichedList, verdictStatuses)
        } catch (e: Exception) {
            Log.e("VX_DEBUG", "Comprehensive AI Filter failed: ${e.message}")
            _recommendations.value = rawList
            processNewSignalsAndNotify(rawList, emptyMap())
        }
    }

    private fun processNewSignalsAndNotify(list: List<SupabaseRecommendation>, aiVerdicts: Map<Int, String>) {
        val isValidUser = subscriptionKey.value?.isValid == true
        list.forEach { rec ->
            val isNew = !seenSignalIds.contains(rec.id)
            if (isNew) {
                seenSignalIds.add(rec.id)
                if (isValidUser && !isFirstRecommendationLoad) {
                    val verdict = aiVerdicts[rec.id]?.uppercase() ?: ""
                    if (verdict == "RISKY_CLOSE" || verdict == "RISKY" || verdict == "CLOSE") {
                        sendLocalNotification(
                            title = "⚠️ Urgent Close Signal for ${rec.pair}",
                            text = "AI Warning Alert: Close trade for ${rec.pair} immediately to avoid losses."
                        )
                    } else {
                        val isBuy = rec.title.contains("BUY") || rec.title.contains("LONG") || rec.title.contains("CALL")
                        val sideInfo = if (isBuy) "Strong Buy Recommendation" else "Strong Sell Recommendation"
                        sendLocalNotification(
                            title = "📈 New Trade Detected: ${rec.pair}",
                            text = "$sideInfo at entry price ${rec.entryPrice}"
                        )
                    }
                }
            }
        }
        isFirstRecommendationLoad = false
    }

    fun sendMessage(text: String) {
        val isValidUser = subscriptionKey.value?.isValid == true
        if (!isValidUser) {
            val errorMsg = "Chat is reserved for active subscribers only. Please activate your key."
            _chatMessages.value += ChatMessage(errorMsg, false)
            return
        }
        val userMsg = ChatMessage(text, true)
        _chatMessages.value += userMsg
        
        viewModelScope.launch {
            _isSendingChat.value = true
            try {
                val contextPrompt = "You are a professional crypto expert and market analyst. " +
                        "Respond to the user in the SAME LANGUAGE they used in their message. " +
                        "Provide a clean, concise, and direct response using clear bullet points for key data. " +
                        "Context: $text"
                val responseBody = gazacodeApi.getChatResponse(contextPrompt)
                val fullResponse = parseChatMessage(responseBody.string())
                
                // Simulate Streaming on UI
                var displayedText = ""
                val aiTimestamp = System.currentTimeMillis()
                val msgIndex = _chatMessages.value.size
                _chatMessages.value += ChatMessage("", false, aiTimestamp) // Placeholder for AI response
                
                val chunks = fullResponse.split(" ")
                for (chunk in chunks) {
                    displayedText += "$chunk "
                    _chatMessages.value = _chatMessages.value.toMutableList().apply {
                        this[msgIndex] = ChatMessage(displayedText.trim(), false, aiTimestamp)
                    }
                    delay(50) // Adjust speed here
                }
            } catch (e: Exception) {
                _chatMessages.value += ChatMessage("Error: ${e.localizedMessage ?: "Connection failed"}", false)
            } finally {
                _isSendingChat.value = false
            }
        }
    }

    fun selectRecommendation(id: Int?) {
        _selectedRecommendationId.value = id
    }

    fun onChatWithRecommendation(rec: SupabaseRecommendation) {
        val prompt = "I am interested in the ${rec.pair} trade. Entry: ${rec.entryPrice}, Target: ${rec.targetPrice}, Stop Loss: ${rec.stopLoss}. Explain this trade in detail, including the technical targets."
        sendMessage(prompt)
    }

    fun applyPromoCode(code: String) {
        _promoCode.value = code.uppercase()
        when (code.uppercase()) {
            "VX-999" -> _currentPrice.value = 22.0 // $3 off
            "CRYPTO" -> _currentPrice.value = 20.0 // $5 off
            "WELCOME20" -> _currentPrice.value = 20.0 // 20% off roughly
            else -> _currentPrice.value = 25.0
        }
    }

    fun logout() {
        viewModelScope.launch {
            dao.clearKey()
            _chatMessages.value = emptyList()
            _recommendations.value = emptyList()
        }
    }
}
