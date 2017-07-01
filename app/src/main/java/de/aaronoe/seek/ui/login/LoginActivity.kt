package de.aaronoe.seek.ui.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import butterknife.ButterKnife
import de.aaronoe.seek.BuildConfig

import de.aaronoe.seek.R
import de.aaronoe.seek.SplashApp
import de.aaronoe.seek.auth.AuthManager
import de.aaronoe.seek.data.model.authorization.AccessToken
import de.aaronoe.seek.data.remote.AuthorizationInterface
import de.aaronoe.seek.ui.mainnav.NavigationActivity
import de.aaronoe.seek.util.bindView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    val clientId = BuildConfig.UNSPLASH_API_KEY
    val clientSecret = BuildConfig.UNSPLASH_CLIENT_SECRET
    lateinit var redirectUri : String
    lateinit var context : Context
    lateinit var authManager : AuthManager

    val loginButton: Button by bindView(R.id.login_button)
    val registerButton: Button by bindView(R.id.register_button)

    @Inject
    lateinit var authService : AuthorizationInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        redirectUri = getString(R.string.unsplash_callback)
        context = this

        (application as SplashApp).netComponent.inject(this)
        authManager = (application as SplashApp).authManager

        ButterKnife.bind(this)

        if (authManager.loggedIn) {
            // User is logged in already
            registerButton.visibility = View.GONE
            loginButton.text = getString(R.string.logout)
            loginButton.setOnClickListener {
                // log user out and leave
                authManager.logout()
                Toast.makeText(this, getString(R.string.loggedoutmessage), Toast.LENGTH_SHORT).show()
                val intent = Intent(context, NavigationActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                finish()
                startActivity(intent)
            }
        } else {
            loginButton.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse((application as SplashApp).getLoginUrl(this)))) }
            registerButton.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SplashApp.UNSPLASH_JOIN_URL))) }
        }

    }

    public override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null
                && intent.data != null
                && !TextUtils.isEmpty(intent.data.authority)
                && SplashApp.UNSPLASH_LOGIN_CALLBACK == intent.data.authority) {
            loginButton.visibility = View.INVISIBLE
            registerButton.visibility = View.INVISIBLE
            val code = intent.data.getQueryParameter("code")
            if(code == null) {
                Toast.makeText(context, getString(R.string.authentication_error), Toast.LENGTH_LONG).show()
                return
            }
            requestAccessToken(code)

        }
    }

    fun requestAccessToken(code : String) {
        val call = authService.getAccessToken(clientId, clientSecret, redirectUri, code, "authorization_code")

        call.enqueue(object : Callback<AccessToken> {
            override fun onResponse(p0: Call<AccessToken>?, response: Response<AccessToken>) {
                if (response.isSuccessful) {
                    Log.d("Token Loaded", response.body().toString())
                    (application as SplashApp).authManager.login(response.body().accessToken)
                    val intent = Intent(context, NavigationActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    Toast.makeText(context, getString(R.string.login_success), Toast.LENGTH_LONG).show()
                    finish()
                    startActivity(intent)
                }
            }

            override fun onFailure(p0: Call<AccessToken>?, p1: Throwable?) {
                Toast.makeText(context, getString(R.string.authentication_error), Toast.LENGTH_LONG).show()
                loginButton.visibility = View.VISIBLE
                registerButton.visibility = View.VISIBLE
            }
        })
    }

}
