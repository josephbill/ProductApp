package com.example.myshoes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myshoes.ui.theme.MyShoesTheme
import com.example.myshoes.vendor.VendorDashboard
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                  MyShoesTheme() {
                      Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                          Scaffold(topBar = {
                              TopAppBar(backgroundColor = Color.Black,
                                        title = {
                                            Text(
                                                text = "Products App",
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center,
                                                color = Color.White
                                                )
                                        })
                          }) {
                              Column(modifier = Modifier.padding(it)) {
                                  authenticationUi(LocalContext.current)
                              }
                          }
                      }
                  }
              }  
        }
    }


@Composable
fun authenticationUi(context : Context){
       // mutableState of to capture text entry
    // phone number ref
    val phoneNumber = remember {
        mutableStateOf("")
    }
    // otp reference
    val otp = remember {
        mutableStateOf("")
    }
    // verification ID if OTP token is valid
    val verificationId = remember {
        mutableStateOf("")
    }
    // custom messages to save/store info for the user
    val message = remember {
        mutableStateOf("")
    }

    // Firebase Initialization
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var callback : PhoneAuthProvider.OnVerificationStateChangedCallbacks

    // create UI : edit text , Button clicks
    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .background(Color.White),
          verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ){
          TextField(
              value = phoneNumber.value,
              onValueChange = { phoneNumber.value = it },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              placeholder = { Text(text = "Enter your phone number")},
              modifier = Modifier
                  .padding(16.dp)
                  .fillMaxWidth(),
              textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
              singleLine =  true
          )
          Spacer(modifier = Modifier.height(10.dp))
          // button to generate otp / make the OTP call
          Button(onClick = {

              // check if the phonenumber variable is empty
              if (TextUtils.isEmpty(phoneNumber.value.toString())){
                  Toast.makeText(context, "Phone Number cannot be empty", Toast.LENGTH_LONG).show()
              } else {
                  // country code
                  val number = "+254${phoneNumber.value}"
                  sendVerificationCode(number,mAuth,context as Activity, callback)
              }

          }) {
                    Text(text = "Get OTP" , modifier = Modifier.padding(8.dp))
          }
          Spacer(modifier = Modifier.height(10.dp))

          // OTP Fields

        TextField(
            value = otp.value,
            onValueChange = { otp.value = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(text = "Enter your OTP code")},
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
            singleLine =  true
        )
        Spacer(modifier = Modifier.height(10.dp))
        // button to generate otp / make the OTP call
        Button(onClick = {
            // check if the phonenumber variable is empty
            if (TextUtils.isEmpty(otp.value.toString())){
                Toast.makeText(context, "OTP cannot be empty", Toast.LENGTH_LONG).show()
            } else {
                 val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId.value, otp.value)
                // login in with the OTP Credentials
                signInWithPhoneAuthCredentials(credential,mAuth,context as Activity, context, message)
            }

        },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Verify OTP" , modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(5.dp))
        
        // Text field to communicate to my user whether the OTP was verified or not 
        Text(text = message.value, style = TextStyle(color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold))


        // interacting with the callback to know the status of the verification
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                message.value = "Verification Successful"
                Toast.makeText(context, "Verification Successful", Toast.LENGTH_LONG).show()

            }

            override fun onVerificationFailed(p0: FirebaseException) {
                message.value = "Verification failed " + p0.message
                Toast.makeText(context, "Verification Failed... ", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationId.value = p0
            }
        }
    }
}


private fun sendVerificationCode(
    number: String,
    mAuth: FirebaseAuth,
    activity: Activity,
    callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
) {
    // generating the actual code
    
    val options = PhoneAuthOptions.newBuilder(mAuth)
        .setPhoneNumber(number)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(callback)
        .build()
    PhoneAuthProvider.verifyPhoneNumber(options)
}


private fun signInWithPhoneAuthCredentials(
    credential: PhoneAuthCredential,
    mAuth: FirebaseAuth,
    activity: Activity,
    context: Activity,
    message: MutableState<String>
) {

    // sign in using a verified OTP code
    mAuth.signInWithCredential(credential).addOnCompleteListener(activity) {
           if (it.isSuccessful){
               message.value = "Verification Successful"
               Toast.makeText(context, "Verification Successful", Toast.LENGTH_LONG).show()
               // go to relevant activity
               goToVendorDashboard(context)

           } else {
               if(it.exception is FirebaseAuthInvalidCredentialsException){
                   Toast.makeText(context, "Verification Failed... " + (it.exception as FirebaseAuthInvalidCredentialsException).message
                       , Toast.LENGTH_LONG).show()
               }
           }
    }

}

fun goToVendorDashboard(context: Activity) {
    val intent = Intent(context, VendorDashboard::class.java)
    context.startActivity(intent)
}































