package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseClient {
    private const val TAG = "SupabaseClient"

    // القيمة الافتراضية المأخوذة من BuildConfig أو التكوين
    var currentBaseUrl: String = try {
        BuildConfig.SUPABASE_URL.ifEmpty { "https://your-project.supabase.co" }
    } catch (_: Exception) {
        "https://your-project.supabase.co"
    }

    var currentAnonKey: String = try {
        BuildConfig.SUPABASE_ANON_KEY.ifEmpty { "placeholder_key" }
    } catch (_: Exception) {
        "placeholder_key"
    }

    // رمز المستخدم المسجل (Bearer token للمدير) إن وجد
    var userBearerToken: String? = null

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()

        // إضافة المفتاح العام لـ Supabase apikey
        builder.header("apikey", currentAnonKey)

        // إذا كان هناك رمز JWT للمستخدم نستخدمه، وإلا نستخدم anon key كـ Bearer
        val token = userBearerToken ?: currentAnonKey
        builder.header("Authorization", "Bearer $token")

        chain.proceed(builder.build())
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun isConfigured(): Boolean {
        val url = currentBaseUrl.trim()
        val key = currentAnonKey.trim()
        return url.isNotEmpty() &&
                !url.contains("your-project.supabase.co") &&
                key.isNotEmpty() &&
                !key.contains("placeholder")
    }

    fun getApi(): SupabaseApi {
        val safeUrl = if (currentBaseUrl.endsWith("/")) currentBaseUrl else "$currentBaseUrl/"
        return try {
            Retrofit.Builder()
                .baseUrl(safeUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SupabaseApi::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Retrofit client: ${e.message}")
            // Fallback in case safeUrl is invalid format
            Retrofit.Builder()
                .baseUrl("https://example.supabase.co/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SupabaseApi::class.java)
        }
    }
}
