package com.app.edcpoc.data.repository

import com.app.edcpoc.data.model.FaceCompareRequest
import com.app.edcpoc.data.model.FaceCompareTencentResponse
import com.app.edcpoc.data.model.KtpResp
import com.app.edcpoc.data.model.KtpReq
import com.app.edcpoc.data.remote.ApiService
import com.app.edcpoc.utils.LogUtils
import com.app.edcpoc.data.model.ApiErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.ResponseBody


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
    private val edcmid_base_url = "https://edcmid.idpay.co.id/"

    suspend fun sendKtpData(param: KtpReq): Result<KtpResp> = withContext(Dispatchers.IO) {
        val api = provideApiService(edcmid_base_url)
        try {
            val response = api.sendKtpData(param)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.status == -1) {
                        Result.failure(Exception(body.message))
                    } else {
                        Result.success(body)
                    }
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = try {
                    Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                } catch (e: Exception) {
                    null
                }
                val errorMsg = errorResponse?.message ?: errorBody ?: "Unknown error"
                Result.failure(Exception(errorMsg))
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
                    if (body.status == -1) {
                        Result.failure(Exception(body.message))
                    } else {
                        Result.success(body)
                    }
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = try {
                    Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                } catch (e: Exception) {
                    null
                }
                val errorMsg = errorResponse?.message ?: errorBody ?: "Unknown error"
                Result.failure(Exception(errorMsg))
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
                    if (body.status == -1) {
                        Result.failure(Exception(body.message))
                    } else {
                        Result.success(body)
                    }
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = try {
                    Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                } catch (e: Exception) {
                    null
                }
                val errorMsg = errorResponse?.message ?: errorBody ?: "Unknown error"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
