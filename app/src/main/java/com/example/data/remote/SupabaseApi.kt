package com.example.data.remote

import com.example.data.model.Category
import com.example.data.model.Zamil
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SupabaseApi {

    @GET("rest/v1/categories")
    suspend fun getCategories(
        @Query("select") select: String = "*",
        @Query("order") order: String = "sort_order.asc"
    ): Response<List<Category>>

    @POST("rest/v1/categories")
    suspend fun insertCategory(
        @Header("Prefer") prefer: String = "return=representation",
        @Body category: Map<String, Any?>
    ): Response<List<Category>>

    @DELETE("rest/v1/categories")
    suspend fun deleteCategory(
        @Query("id") idFilter: String
    ): Response<ResponseBody>

    @GET("rest/v1/zawamil")
    suspend fun getZawamil(
        @Query("select") select: String = "*",
        @Query("order") order: String = "sort_order.asc"
    ): Response<List<Zamil>>

    @POST("rest/v1/zawamil")
    suspend fun insertZamil(
        @Header("Prefer") prefer: String = "return=representation",
        @Body zamil: Map<String, Any?>
    ): Response<List<Zamil>>

    @PATCH("rest/v1/zawamil")
    suspend fun updateZamil(
        @Query("id") idFilter: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body updates: Map<String, Any?>
    ): Response<List<Zamil>>

    @DELETE("rest/v1/zawamil")
    suspend fun deleteZamil(
        @Query("id") idFilter: String
    ): Response<ResponseBody>

    @POST("rest/v1/rpc/reorder_zamil")
    suspend fun reorderZamil(
        @Body params: Map<String, String>
    ): Response<ResponseBody>

    @POST("rest/v1/rpc/record_zamil_play")
    suspend fun recordZamilPlay(
        @Body params: Map<String, String>
    ): Response<ResponseBody>

    @GET("rest/v1/favorites")
    suspend fun getUserFavorites(
        @Query("select") select: String = "zamil_id"
    ): Response<List<Map<String, String>>>

    @POST("rest/v1/favorites")
    suspend fun addFavorite(
        @Body payload: Map<String, String>,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates"
    ): Response<ResponseBody>

    @DELETE("rest/v1/favorites")
    suspend fun removeFavorite(
        @Query("zamil_id") zamilIdFilter: String
    ): Response<ResponseBody>

    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun uploadFile(
        @Path("bucket") bucket: String,
        @Path("path") path: String,
        @Body fileBytes: RequestBody
    ): Response<ResponseBody>
}
