package com.projects.plantie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.amplifyframework.core.Amplify
import org.tensorflow.lite.examples.plantie.R

class MainActivity : AppCompatActivity() {
    private var openCameraButton: Button? = null
    private var loginPageButton: Button? = null
    private var signUpPageButton: Button? = null
    private var logoutButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openCameraButton = findViewById<Button>(R.id.button_open_camera)
        loginPageButton = findViewById<Button>(R.id.button_main_to_login_page)
        signUpPageButton = findViewById<Button>(R.id.button_main_to_sign_up_page)
        logoutButton = findViewById<Button>(R.id.button_main_logout)

        openCameraButton!!.setOnClickListener{ openCameraPage() }
        loginPageButton!!.setOnClickListener{ openLoginPage() }
        signUpPageButton!!.setOnClickListener{ openSignUpPage() }
        logoutButton!!.setOnClickListener{ logout() }

        //amplify login check
        Amplify.Auth.fetchAuthSession(
            {
                Log.i("AmplifyCheckLogin", "Auth session = $it")
                if (it.isSignedIn){
                    //TODO if signed in change main page login/signup to just one logout button
                }
            },
            { error -> Log.e("AmplifyCheckLogin", "Failed to fetch auth session", error) }
        )
    }

    private fun openCameraPage() {
        intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    private fun openLoginPage() {
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun openSignUpPage() {
        intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        Amplify.Auth.signOut(
            {
                Log.i("AuthQuickstart", "Signed out successfully")
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_SHORT).show()
                }
            },
            { Log.e("AuthQuickstart", "Sign out failed", it) }
        )
    }
}