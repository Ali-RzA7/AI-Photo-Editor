package com.example.myapplication.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Clipdrop API Retrofit Interface.
 * 4 endpoint: cleanup, remove-background, replace-background, upscale.
 */
public interface ClipDropApiService {

    /**
     * Nesne/Kişi Silme (Cleanup / Inpainting)
     * Multipart: image_file (PNG/JPG) + mask_file (Siyah-Beyaz PNG)
     */
    @Multipart
    @POST("cleanup/v1")
    Call<ResponseBody> cleanup(
            @Part MultipartBody.Part imageFile,
            @Part MultipartBody.Part maskFile
    );

    /**
     * Arka Plan Silme (Remove Background)
     * Multipart: image_file (PNG/JPG)
     */
    @Multipart
    @POST("remove-background/v1")
    Call<ResponseBody> removeBackground(
            @Part MultipartBody.Part imageFile
    );

    /**
     * Görüntü Kalite Artırma (Image Upscaling)
     * Multipart: image_file (PNG/JPG) + target_width + target_height
     */
    @Multipart
    @POST("image-upscaling/v1/upscale")
    Call<ResponseBody> upscale(
            @Part MultipartBody.Part imageFile,
            @Part("target_width") RequestBody targetWidth,
            @Part("target_height") RequestBody targetHeight
    );

    /**
     * Arka Plan Değiştirme (Replace Background)
     * Multipart: image_file (PNG/JPG, max 2048x2048) + prompt (Text)
     */
    @Multipart
    @POST("replace-background/v1")
    Call<ResponseBody> replaceBackground(
            @Part MultipartBody.Part imageFile,
            @Part("prompt") RequestBody prompt
    );
}
