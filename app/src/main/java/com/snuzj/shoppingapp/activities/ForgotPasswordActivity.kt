package com.snuzj.shoppingapp.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.snuzj.shoppingapp.Utils
import com.snuzj.shoppingapp.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private companion object{
        private const val TAG = "FORGOT_PASSWORD"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Đang tải...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }


    }

    private var email = ""

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()

        Log.d(TAG, "validateData: email : $email")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error = "Email không đúng"
            binding.emailEt.requestFocus()
        }
        else{
            sendPasswordRecoveryIns()
        }
    }

    private fun sendPasswordRecoveryIns() {
        Log.d(TAG, "sendPasswordRecoveryIns: ")

        //show progress
        progressDialog.setMessage("Link thay đổi mật khẩu đang được gửi tới mail của bạn")
        progressDialog.show()

        //send
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Log.d(TAG, "sendPasswordRecoveryIns: success")
                progressDialog.dismiss()

                Utils.toast(this,"Gửi thành công đến $email")
            }
            .addOnFailureListener { e->
                Log.e(TAG, "sendPasswordRecoveryIns: ", e)

                progressDialog.dismiss()
                Utils.toast(this,"Gửi thất bại vì ${e.message}")
            }
    }
}