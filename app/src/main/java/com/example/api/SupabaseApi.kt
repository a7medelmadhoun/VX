package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header

@JsonClass(generateAdapter = true)
data class SupabaseRecommendation(
    @Json(name = "id") val id: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "pair") val pair: String,
    @Json(name = "entry_price") val entryPrice: String,
    @Json(name = "target_price") val targetPrice: String,
    @Json(name = "stop_loss") val stopLoss: String,
    @Json(name = "is_active") val isActive: Boolean = true
)

interface SupabaseApi {
    @GET("rest/v1/recommendations?select=*")
    suspend fun getRecommendations(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<SupabaseRecommendation>
}
