package com.app.edcpoc.data.repository

import com.app.edcpoc.MyApp
import com.app.edcpoc.data.model.FaceCompareRequest
import com.app.edcpoc.data.model.FaceCompareTencentResponse
import com.app.edcpoc.data.model.KtpResp
import com.app.edcpoc.data.model.KtpReq
import com.app.edcpoc.data.remote.ApiService
import com.app.edcpoc.utils.LogUtils
import com.app.edcpoc.data.model.ApiErrorResponse
import com.app.edcpoc.data.model.LogResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.ResponseBody
import retrofit2.Response
import com.app.edcpoc.PreferenceManager
import com.app.edcpoc.utils.Utility.getSn


private fun provideApiService(baseUrl: String): ApiService {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    val context = MyApp.getContext()
    val preferenceManager = PreferenceManager
    val svpUserId = preferenceManager.getSvpUserId(context)

    val headerInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-Pinpad-Id", getSn())
            .addHeader("X-User-Id", svpUserId ?: "")
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

    fun <T> responseApi(response: Response<T>): Result<T> {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                val status = try {
                    body!!::class.java.getDeclaredField("status").get(body) as? Int
                } catch (e: Exception) { null }
                val message = try {
                    body!!::class.java.getDeclaredField("message").get(body) as? String
                } catch (e: Exception) { null }
                if (status == -1) {
                    return Result.failure(Exception(message ?: "Unknown error"))
                } else {
                    return Result.success(body)
                }
            } else {
                return Result.failure(Exception("Response body is null"))
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorResponse = try {
                Gson().fromJson(errorBody, ApiErrorResponse::class.java)
            } catch (e: Exception) {
                null
            }
            val errorMsg = errorResponse?.message ?: errorBody ?: "Unknown error"
            return Result.failure(Exception(errorMsg))
        }
    }

    suspend fun sendKtpData(param: KtpReq): Result<KtpResp> = withContext(Dispatchers.IO) {
        val api = provideApiService(edcmid_base_url)
        try {
            val response = api.sendKtpData(param)
            responseApi(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendKtpDataSpv(param: KtpReq): Result<KtpResp> = withContext(Dispatchers.IO) {
        val api = provideApiService(edcmid_base_url)
        try {
            val response = api.sendKtpDataSpv(param)
            responseApi(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun faceCompare(param: FaceCompareRequest): Result<FaceCompareTencentResponse> = withContext(Dispatchers.IO) {
        val api = provideApiService(tencent_base_url)
        try {
            val response = api.faceCompare(param)
            responseApi(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFingerLogData(param: KtpReq): Result<LogResponse> = withContext(Dispatchers.IO) {
        val api = provideApiService(edcmid_base_url)
        try {
            val response = api.logFingerprint(param)
            responseApi(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFaceCompareLogData(param: KtpReq): Result<LogResponse> = withContext(Dispatchers.IO) {
        val api = provideApiService(edcmid_base_url)
        try {
            val response = api.logFaceCompare(param)
            responseApi(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
