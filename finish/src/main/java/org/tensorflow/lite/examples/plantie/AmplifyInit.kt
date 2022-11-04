package org.tensorflow.lite.examples.plantie

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify

class AmplifyInit: Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("AmplifyInit", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("AmplifyInit", "Could not initialize Amplify", error)
        }
    }
}