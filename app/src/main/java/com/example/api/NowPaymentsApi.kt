package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class CreateInvoiceRequest(
    @Json(name = "price_amount") val priceAmount: Double,
    @Json(name = "price_currency") val priceCurrency: String = "usd",
    @Json(name = "order_id") val orderId: String? = null,
    @Json(name = "order_description") val orderDescription: String = "VIP Access Signals",
    @Json(name = "ipn_callback_url") val ipnUrl: String = "https://generate-key.gazacode.workers.dev/api/keys/list",
    @Json(name = "success_url") val successUrl: String? = null,
    @Json(name = "cancel_url") val cancelUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateInvoiceResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "order_id") val orderId: String? = null,
    @Json(name = "order_description") val orderDescription: String? = null,
    @Json(name = "price_amount") val priceAmount: String? = null,
    @Json(name = "price_currency") val priceCurrency: String? = null,
    @Json(name = "invoice_url") val invoiceUrl: String? = null
)

interface NowPaymentsApi {
    @POST("v1/invoice")
    suspend fun createInvoice(
        @Header("x-api-key") apiKey: String,
        @Body request: CreateInvoiceRequest
    ): CreateInvoiceResponse
}
