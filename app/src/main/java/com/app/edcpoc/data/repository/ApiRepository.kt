package com.app.edcpoc.data.repository

import com.app.edcpoc.data.model.FaceCompareRequest
import com.app.edcpoc.data.model.FaceCompareTencentResponse
import com.app.edcpoc.data.model.KtpResp
import com.app.edcpoc.data.model.KtpReq
import com.app.edcpoc.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor

private fun provideApiService(baseUrl: String): ApiService {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    val headerInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-Pinpad-Id", "")
            .addHeader("X-User-Id", "")
            .addHeader("X-Transaction-Type", "")
            .build()
        chain.proceed(request)
    }
    val client = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(logging)
        .build()
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}

class ApiRepository {
    private val tencent_base_url = "https://demo-faceapi.idpay.co.id/"
    private val edcmid_base_url = "https://edcmid-central.idpay.co.id/"

    suspend fun sendKtpData(param: KtpReq): Result<KtpResp> = withContext(Dispatchers.IO) {
        val api = provideApiService(edcmid_base_url)
        try {
            val response = api.sendKtpData(param)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("Response error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendKtpDataSpv(param: KtpReq): Result<KtpResp> = withContext(Dispatchers.IO) {
        val api = provideApiService(edcmid_base_url)
        try {
            val response = api.sendKtpDataSpv(param)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("Response error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun faceCompare(param: FaceCompareRequest): Result<FaceCompareTencentResponse> = withContext(Dispatchers.IO) {
        val api = provideApiService(tencent_base_url)
        try {
            val response = api.faceCompare(param)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("Response error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
