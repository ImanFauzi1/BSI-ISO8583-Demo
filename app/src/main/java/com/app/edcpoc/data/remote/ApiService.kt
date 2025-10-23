package com.app.edcpoc.data.remote

import com.app.edcpoc.data.model.FaceCompareRequest
import com.app.edcpoc.data.model.FaceCompareTencentResponse
import com.app.edcpoc.data.model.KtpGetResp
import com.app.edcpoc.data.model.KtpReq
import com.app.edcpoc.data.model.KtpResp
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("edcmid-central/ktp2")
    suspend fun getKtpData(@Query("nik") nik: String): Response<KtpGetResp>
    @POST("edcmid-central/ktp2")
    suspend  fun sendKtpData(@Body ktpReq: KtpReq): Response<KtpResp>

    @POST("edcmid-central/ktp2-spv")
    suspend fun sendKtpDataSpv(@Body ktpReq: KtpReq): Response<KtpResp>

    @POST("facecompare/compare")
    suspend fun faceCompare(@Body body: FaceCompareRequest): Response<FaceCompareTencentResponse>
}

