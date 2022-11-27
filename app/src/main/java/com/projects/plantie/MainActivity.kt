package com.projects.plantie

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.core.Amplify
import org.tensorflow.lite.examples.plantie.R


class MainActivity : AppCompatActivity() {
    private var openCameraButton: Button? = null
    private var openBrowseButton: Button? = null
    private var loginPageButton: Button? = null
    private var signUpPageButton: Button? = null
    private var logoutButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openCameraButton = findViewById<Button>(R.id.button_open_camera)
        openBrowseButton = findViewById<Button>(R.id.button_open_browse)
        loginPageButton = findViewById<Button>(R.id.button_main_to_login_page)
        signUpPageButton = findViewById<Button>(R.id.button_main_to_sign_up_page)
        logoutButton = findViewById<Button>(R.id.button_main_logout)

        openCameraButton!!.setOnClickListener{ openCameraPage() }
        openBrowseButton!!.setOnClickListener{ openBrowsePage() }
        loginPageButton!!.setOnClickListener{ openLoginPage() }
        signUpPageButton!!.setOnClickListener{ openSignUpPage() }
        logoutButton!!.setOnClickListener{ logout() }

        //amplify login check

            Amplify.Auth.fetchAuthSession(
                {
                    Log.i("AmplifyCheckLogin", "Auth session = $it")
                    runOnUiThread(Runnable {
                        if (it.isSignedIn){
                            logoutButton!!.visibility = View.VISIBLE;
                            signUpPageButton!!.visibility = View.GONE;
                            loginPageButton!!.visibility = View.GONE;
                        }else{
                            logoutButton!!.visibility = View.GONE;
                            signUpPageButton!!.visibility = View.VISIBLE;
                            loginPageButton!!.visibility = View.VISIBLE;
                        }
                    })
                },
                { error -> Log.e("AmplifyCheckLogin", "Failed to fetch auth session", error) }
            )


    }

    private fun openCameraPage() {
        intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    private fun openBrowsePage() {
        intent = Intent(this, BrowseActivity::class.java)
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
                    Amplify.Auth.fetchAuthSession(
                        {
                            Log.i("AmplifyCheckLogin", "Auth session = $it")
                            runOnUiThread(Runnable {
                                if (it.isSignedIn){
                                    logoutButton!!.visibility = View.VISIBLE;
                                    signUpPageButton!!.visibility = View.GONE;
                                    loginPageButton!!.visibility = View.GONE;
                                }else{
                                    logoutButton!!.visibility = View.GONE;
                                    signUpPageButton!!.visibility = View.VISIBLE;
                                    loginPageButton!!.visibility = View.VISIBLE;
                                }
                            })
                        },
                        { error -> Log.e("AmplifyCheckLogin", "Failed to fetch auth session", error) }
                    )
                    //val preferences = getSharedPreferences("Plantie", 0)
                    //preferences.edit().clear().commit()
                }
            },
            { Log.e("AuthQuickstart", "Sign out failed", it) }
        )
    }
}