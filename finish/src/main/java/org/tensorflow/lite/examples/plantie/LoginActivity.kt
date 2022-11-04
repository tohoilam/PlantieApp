package org.tensorflow.lite.examples.plantie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.amplifyframework.core.Amplify

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
                if (result.isSignInComplete) {
                    Log.i("AuthQuickstart", "Sign in succeeded")
                    toMainPage()
                } else {
                    Log.i("AuthQuickstart", "Sign in not complete")
                }
            },
            { Log.e("AuthQuickstart", "Failed to sign in", it) }
        )

        var messageString = StringBuilder()
        messageString.append("Username: ").append(username).append("    Password: ").append(password)
        Toast.makeText(this@LoginActivity, messageString, Toast.LENGTH_LONG).show()
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