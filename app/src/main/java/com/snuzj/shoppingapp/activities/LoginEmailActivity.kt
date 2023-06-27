package com.snuzj.shoppingapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.snuzj.shoppingapp.Utils
import com.snuzj.shoppingapp.databinding.ActivityLoginEmailBinding

class LoginEmailActivity : AppCompatActivity() {


    private companion object{
        private const val TAG = "LOGIN_TAG"

    }

    private lateinit var binding: ActivityLoginEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get instance of firebaseAuth for auth related task
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress to show while sign in
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Đang tải...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle toolbarBackBtn click, go back
        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, go to register with email
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterEmailActivity::class.java))
        }

        //handle click, start login
        binding.loginBtn.setOnClickListener{
            validateData()
        }


    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        //input data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        Log.d(TAG, "validateData: email= $email")
        Log.d(TAG, "validateData: password= $password")

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error = "Lỗi định dạng email"
            binding.emailEt.requestFocus()
        }
        else if(password.isEmpty()){
            binding.passwordEt.error = "Hãy nhập mật khẩu của bạn"
            binding.passwordEt.requestFocus()
        }
        else{
            //email pattern is valid and pass is entered. start login
            loginUser()
        }
    }

    private fun loginUser() {
        Log.d(TAG, "loginUser: ")

        //show progress
        progressDialog.setMessage("Đang đăng nhập")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "loginUser: Logged In...")
                progressDialog.dismiss()

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                Log.e(TAG, "loginUser: ",e)
                progressDialog.dismiss()

                Utils.toast(this, "Không thể đăng nhập vì ${e.message}")
            }
    }


}
