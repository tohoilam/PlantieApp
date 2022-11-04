package com.projects.plantie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import org.tensorflow.lite.examples.plantie.R


class SignUpActivity : AppCompatActivity() {

    private var emailEditText: EditText? = null
    private var usernameEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var passwordCheckEditText: EditText? = null
    private var confirmationEditText: EditText? = null
    private var signUpButton: Button? = null
    private var toLoginPageButton: Button? = null
    private var toMainPageButton: Button? = null
    private var confirmationButton: Button? = null
    private var popupView: View? = null
    private var popupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        emailEditText = findViewById<EditText>(R.id.input_sign_up_email)
        usernameEditText = findViewById<EditText>(R.id.input_sign_up_username)
        passwordEditText = findViewById<EditText>(R.id.input_sign_up_password)
        passwordCheckEditText = findViewById<EditText>(R.id.input_sign_up_password_repeat)
        signUpButton = findViewById<Button>(R.id.button_sign_up_submit)
        toLoginPageButton = findViewById<Button>(R.id.button_sign_up_to_login_page)
        toMainPageButton = findViewById<Button>(R.id.button_sign_up_to_main_page)

        signUpButton!!.setOnClickListener{ signUp() }
        toLoginPageButton!!.setOnClickListener{ toLoginPage() }
        toMainPageButton!!.setOnClickListener{ toMainPage() }

        // dismiss the popup window when touched
        val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.popup_window, null)
        popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupView!!.setOnTouchListener { v, _ ->
            popupWindow!!.dismiss()
            true
        }

    }

    private fun signUp() {
        val email = emailEditText!!.text.toString()
        val username = usernameEditText!!.text.toString()
        val password = passwordEditText!!.text.toString()
        val passwordCheck = passwordCheckEditText!!.text.toString()

        //TODO password check be4 invoking below

        //amplify signup
        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.email(), email)
            .build()
        Amplify.Auth.signUp(username, password, options,
            {
            Log.i("AuthQuickStart", "Sign up succeeded: $it")
                /*
                popupWindow!!.showAtLocation(signUpButton, Gravity.CENTER, 0, 0)
                confirmationButton = findViewById<Button>(R.id.button_signup_submitConfirmation)
                confirmationEditText = findViewById<EditText>(R.id.input_signup_confirmation)
                confirmationButton!!.setOnClickListener{
                    val confirmationCode = confirmationEditText!!.text.toString()
                    Amplify.Auth.confirmSignUp(username, confirmationCode,
                        { result ->
                            if (result.isSignUpComplete) {
                                Log.i("AuthQuickstart", "Confirm signUp succeeded")
                            } else {
                                Log.i("AuthQuickstart","Confirm sign up not complete")
                            }
                        },
                        { Log.e("AuthQuickstart", "Failed to confirm sign up", it) }
                    )
                }*/
            },
            { Log.e ("AuthQuickStart", "Sign up failed", it) }
        )
    }


    private fun toLoginPage() {
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun toMainPage() {
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}