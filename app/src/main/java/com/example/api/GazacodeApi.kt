package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class KeyValidationResponse(
    @Json(name = "valid") val isValid: Boolean,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    @Json(name = "response") val response: String
)

@JsonClass(generateAdapter = true)
data class Recommendation(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "symbol") val symbol: Any? = null,
    @Json(name = "type") val type: Any? = null,
    @Json(name = "side") val side: Any? = null,
    @Json(name = "action") val action: Any? = null,
    @Json(name = "entry") val entry: Any? = null,
    @Json(name = "opening") val opening: Any? = null,
    @Json(name = "entry_price") val entryPrice: Any? = null,
    @Json(name = "target") val target: Any? = null,
    @Json(name = "tp") val tp: Any? = null,
    @Json(name = "target_price") val targetPrice: Any? = null,
    @Json(name = "stop_loss") val stopLoss: Any? = null,
    @Json(name = "sl") val sl: Any? = null,
    @Json(name = "take_profit") val takeProfit: Any? = null,
    @Json(name = "entry_zone") val entryZone: Any? = null,
    @Json(name = "exit_price") val exitPrice: Any? = null,
    @Json(name = "goal") val goal: Any? = null,
    @Json(name = "timestamp") val timestamp: Any? = null
)

interface GazacodeApi {
    @GET("/api/keys/validate/{key}")
    suspend fun validateKey(@Path("key") key: String): KeyValidationResponse

    @GET("/chat")
    suspend fun getChatResponse(@Query("q") query: String): ResponseBody

    @POST("/analyzer")
    suspend fun getRecommendations(): ResponseBody

    @GET("/api/keys/list")
    suspend fun getKeysList(): ResponseBody

    @POST("/api/keys/generate")
    suspend fun generateKey(@Query("email") email: String? = null): ResponseBody
}
