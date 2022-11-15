package com.projects.plantie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.amplifyframework.core.Amplify
import org.tensorflow.lite.examples.plantie.R

class LoginActivity : AppCompatActivity() {
    private var usernameEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var loginButton: Button? = null
    private var toSignUpButton: Button? = null
    private var toMainPageButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById<EditText>(R.id.input_login_username)
        passwordEditText = findViewById<EditText>(R.id.input_login_password)
        loginButton = findViewById<Button>(R.id.button_login_submit)
        toSignUpButton = findViewById<Button>(R.id.button_login_to_sign_up_page)
        toMainPageButton = findViewById<Button>(R.id.button_login_to_main_page)

        loginButton!!.setOnClickListener{ login() }
        toSignUpButton!!.setOnClickListener{ toSignUpPage() }
        toMainPageButton!!.setOnClickListener{ toMainPage() }
    }

    private fun login() {
        val username = usernameEditText!!.text.toString()
        val password = passwordEditText!!.text.toString()

        //Amplify Signin
        Amplify.Auth.signIn(username, password,
            { result ->
                Log.i("Amplify", "result:"+result)
                if (result.isSignInComplete) {
                    Log.i("AuthQuickstart", "Sign in succeeded")
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(getApplicationContext(), "Logined", Toast.LENGTH_SHORT).show()
                    }
                    toMainPage()
                } else {
                    Log.i("AuthQuickstart", "Sign in not complete")
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(getApplicationContext(), "User not confirmed, please activate your account in email", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            {
                Log.e("AuthQuickstart", "Failed to sign in", it)
                Log.i("AuthException", it.message.toString())
                var msg = ""
                when (it.message){
                    "Username already exists in the system." -> msg = "Username exists, please try with another username"
                    "Failed since user is not authorized." -> msg = "Incorrect password, please try again"
                    "User not found in the system." -> msg = "User not found, please signin if you are new user"
                    else -> msg = "Fatal error"
                }
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        )

        var messageString = StringBuilder()
        messageString.append("Username: ").append(username).append("    Password: ").append(password)
        //Toast.makeText(this@LoginActivity, messageString, Toast.LENGTH_LONG).show()
    }

    private fun toSignUpPage() {
        intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun toMainPage() {
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}