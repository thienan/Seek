package de.aaronoe.seek.auth

import android.util.Log
import de.aaronoe.seek.SplashApp
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 *
 * Created by private on 29.06.17.
 */
class AuthenticationInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.e("AuthIntercept", "Logged in: " + SplashApp.getInstance().authManager.token)
        val request: Request = if (SplashApp.getInstance().authManager.loggedIn) {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer " + SplashApp.getInstance().authManager.token)
                .build()
        } else {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Client-ID " + SplashApp.CLIENT_ID)
                .build()
        }
        return chain.proceed(request)
    }
}