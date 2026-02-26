package com.example.myapplication.api;

import android.util.Log;

import com.example.myapplication.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Retrofit Client singleton.
 * OkHttp ile API Key interceptor, 402 fallback ve logging.
 *
 * 402 (kredi bitti) alındığında otomatik olarak yedek API anahtarına geçer
 * ve bundan sonra hep yedek anahtarı kullanmaya devam eder.
 */
public class RetrofitClient {

    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "https://clipdrop-api.co/";
    private static RetrofitClient instance;
    private final ClipDropApiService apiService;

    /**
     * true olduğunda artık hep CLIPDROP_API_KEY1 kullanılır.
     * Thread-safe: birden fazla istek aynı anda çalışabilir.
     */
    private static final AtomicBoolean useFallbackKey = new AtomicBoolean(false);

    private RetrofitClient() {
        // Logging Interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        // OkHttp Client - API Key interceptor + 402 fallback
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();

                    // Hangi anahtarı kullanacağımızı belirle
                    String apiKey = useFallbackKey.get()
                            ? BuildConfig.CLIPDROP_API_KEY1
                            : BuildConfig.CLIPDROP_API_KEY;

                    Request request = original.newBuilder()
                            .header("x-api-key", apiKey)
                            .method(original.method(), original.body())
                            .build();

                    Response response = chain.proceed(request);

                    // 402 kontrolü — kredi bitti, yedek anahtara geç
                    if (response.code() == 402 && !useFallbackKey.get()) {
                        Log.w(TAG, "402 alındı — yedek API anahtarına geçiliyor...");

                        // Eski response'u kapat (hafıza sızıntısı önleme)
                        response.close();

                        // Kalıcı olarak yedek anahtara geç
                        useFallbackKey.set(true);

                        // Aynı isteği yedek anahtarla tekrarla
                        Request fallbackRequest = original.newBuilder()
                                .header("x-api-key", BuildConfig.CLIPDROP_API_KEY1)
                                .method(original.method(), original.body())
                                .build();

                        return chain.proceed(fallbackRequest);
                    }

                    return response;
                })
                .addInterceptor(loggingInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        // Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .build();

        apiService = retrofit.create(ClipDropApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ClipDropApiService getApiService() {
        return apiService;
    }
}
