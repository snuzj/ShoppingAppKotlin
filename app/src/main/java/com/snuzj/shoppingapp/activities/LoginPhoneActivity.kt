@file:Suppress("DEPRECATION")

package com.snuzj.shoppingapp.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.FirebaseDatabase
import com.snuzj.shoppingapp.Utils
import com.snuzj.shoppingapp.databinding.ActivityLoginPhoneBinding
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPhoneBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var forceRefreshingToken: ForceResendingToken? = null
    private lateinit var mCallbacks : OnVerificationStateChangedCallbacks
    private var mVerificationId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneInputRl.visibility = View.VISIBLE
        binding.otpInputRl.visibility = View.INVISIBLE

        //show progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Đang tải...")
        progressDialog.setCanceledOnTouchOutside(false)

        //firebase auth for related tasks
        firebaseAuth = FirebaseAuth.getInstance()

        //listen for phone login callbacks.
        phoneLoginCallBacks()

        //handle click, send otp
        binding.sendOtpBtn.setOnClickListener {
            validateData()
        }

        //handle click, resend otp
        binding.resendOtpTv.setOnClickListener {
            resendVerificationCode(forceRefreshingToken)
        }

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, verifyOtp
        binding.verifyOtpBtn.setOnClickListener {

            val otp = binding.otpEt.text.toString().trim()
            Log.d(TAG, "onCreate: otp: $otp")

            if(otp.isEmpty()){
                binding.otpEt.error = "Hãy nhập OTP"
                binding.otpEt.requestFocus()
            }
            else if(otp.length<6){
                binding.otpEt.error = "Mã OTP có 6 chữ số"
                binding.otpEt.requestFocus()
            }
            else{
                verifyPhoneNumberWithCode(mVerificationId, otp)
            }
        }



    }

    private var phoneCode = ""
    private var phoneNumber = ""
    private var phoneNumberWithCode = ""

    private fun validateData(){
        phoneCode = binding.phoneCodeTil.selectedCountryCodeWithPlus
        phoneNumber = binding.phoneNumberEt.text.toString().trim()
        phoneNumberWithCode = phoneCode + phoneNumber

        Log.d(TAG, "validateData: phoneCode: $phoneCode")
        Log.d(TAG, "validateData: phoneNumb: $phoneNumber")
        Log.d(TAG, "validateData: phoneNumberWithCode: $phoneNumberWithCode")

        if(phoneNumber.isEmpty()){
            binding.phoneNumberEt.error = "Nhập số điện thoại"
            binding.phoneNumberEt.requestFocus()
        }
        else{
            startPhoneNumberVerification()
        }
    }

    private fun startPhoneNumberVerification() {
        Log.d(TAG, "startPhoneNumberVerification: ")
        progressDialog.setMessage("OTP được gửi tới số $phoneNumberWithCode")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth) //firebase auth instance
            .setPhoneNumber(phoneNumberWithCode) //phone number
            .setTimeout(60L,TimeUnit.SECONDS) //timeout
            .setActivity(this) //activity for callback binding
            .setCallbacks(mCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun phoneLoginCallBacks() {
        Log.d(TAG, "phoneLoginCallBacks: ")
        mCallbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: ")
                /*this callback will be invoked in 2 situations:
                * 1.Instant Verification
                * 2.Auto retrieval
                */
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                /*this callback is invoked in an invalid request for verification is made
                * for instance if the phone number format isn't invalid*/
                Log.e(TAG, "onVerificationFailed: ", e)

                progressDialog.dismiss()
                Utils.toast(this@LoginPhoneActivity, "${e.message}")
            }

            @SuppressLint("SetTextI18n")
            override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
                /*The SMS verification code has been sent to the provided phone number,
                User need to enter code and then construct a credential by combining the code with a verificationId
                 */
                Log.d(TAG, "onCodeSent: verificationId: $verificationId")

                mVerificationId = verificationId
                forceRefreshingToken = token

                progressDialog.dismiss()

                binding.phoneInputRl.visibility = View.INVISIBLE
                binding.otpInputRl.visibility = View.VISIBLE

                Utils.toast(
                    this@LoginPhoneActivity,
                    "Mã xác nhận OTP đã được gửi $phoneNumberWithCode"
                )

                binding.loginLabelTv.text = "Hãy nhập mã OTP được gửi tới số $phoneNumberWithCode"
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {

            }

        }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, otp: String) {
        Log.d(TAG, "verifyPhoneNumberWithCode: VerificationId: $verificationId")
        Log.d(TAG, "verifyPhoneNumberWithCode: otp: $otp")

        progressDialog.setMessage("Đang xác nhận mã OTP")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId!!,otp)

        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: ")

        progressDialog.setMessage("Đang đăng nhập")
        progressDialog.show()

        firebaseAuth.signInWithCredential(credential)
            .addOnFailureListener { e->
                Log.e(TAG, "signInWithPhoneAuthCredential: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Đăng nhập thất bại vì ${e.message}")
            }
            .addOnSuccessListener { authResult->
                Log.d(TAG, "signInWithPhoneAuthCredential: Success")

                if (authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "signInWithPhoneAuthCredential: New User, Account Created")
                    updateUserInfoDb()
                }
                else{
                    Log.d(TAG, "signInWithPhoneAuthCredential: Existing User Logged In")
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
    }

    private fun updateUserInfoDb() {
        Log.d(TAG, "updateUserInfoDb: ")

        progressDialog.setMessage("Saving User Info")
        progressDialog.show()

        val timestamp = Utils.getTimeStamp()
        val registeredUserUid = firebaseAuth.uid
        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = ""
        hashMap["phonecode"] = phoneCode
        hashMap["phoneNumber"] = phoneNumber
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Phone"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = ""
        hashMap["uid"] = "$registeredUserUid"

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfoDb: user info updated")
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                Log.e(TAG, "updateUserInfoDb: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Lưu thông tin người dùng thất bại vì ${e.message}")
            }
    }

    private fun resendVerificationCode(token: ForceResendingToken?) {
        Log.d(TAG, "resendVerificationCode: ")

        progressDialog.setMessage("OTP được gửi tới số $phoneNumberWithCode")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth) //firebase auth instance
            .setPhoneNumber(phoneNumberWithCode) //phone number
            .setTimeout(60L,TimeUnit.SECONDS) //timeout
            .setActivity(this) //activity for callback binding
            .setCallbacks(mCallbacks)
            .setForceResendingToken(token!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }



    private companion object{
        private const val TAG = "PHONE_LOGIN_TAG"
    }
}