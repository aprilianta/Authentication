package com.grd.authentication.Retrofit

import com.grd.authentication.Model.LoginModel
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Api {
    @FormUrlEncoded
    @POST("login.php")
    fun cek(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginModel>
}