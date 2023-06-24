package com.snuzj.shoppingapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.snuzj.shoppingapp.databinding.ActivityLoginEmailBinding
import com.snuzj.shoppingapp.databinding.ActivityRegisterEmailBinding

class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterEmailBinding

    private companion object{
        private const val TAG = "REGISTER_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Đang tải")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.haveAccountTv.setOnClickListener {
            onBackPressed()
        }

        binding.registerBtn.setOnClickListener {
            validateData()
        }

    }

    private var email = ""
    private var password = ""
    private var cPassword = ""

    private fun validateData() {
        //input data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        cPassword = binding.cPasswordEt.text.toString().trim()

        Log.d(TAG, "validateData: email : $email")
        Log.d(TAG, "validateData: password : $password")
        Log.d(TAG, "validateData: confirm password : $cPassword")

        //validate data
        when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailEt.error = "Lỗi định dạng email"
                binding.emailEt.requestFocus()
            }
            password.isEmpty() -> {
                binding.passwordEt.error = "Hãy nhập mật khẩu"
                binding.passwordEt.requestFocus()
            }
            cPassword.isEmpty() -> {
                binding.cPasswordEt.error = "Hãy xác nhận mật khẩu"
                binding.cPasswordEt.requestFocus()
            }
            password != cPassword -> {
                binding.cPasswordEt.error = "Mật khẩu không khớp"
                binding.cPasswordEt.requestFocus()
            }
            else -> {
                registerUser()
            }
        }



    }

    private fun registerUser() {
        Log.d(TAG, "registerUser: ")

        //show progress
        progressDialog.setMessage("Đang tạo tài khoản")
        progressDialog.show()

        //start user sign-up
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //if success, set info to firebase db
                Log.d(TAG, "registerUser: Register Success")
                updateUserInfo()
            }
            .addOnFailureListener {e->
                Log.d(TAG, "registerUser: ",e)
                progressDialog.dismiss()
                Utils.toast(this,"Tạo tài khoản thất bại vì ${e.message}")
            }
    }

    private fun updateUserInfo() {
        Log.d(TAG, "updateUserInfo: ")
        //show progress dialog
        progressDialog.setMessage("Đang lưu thông tin người dùng")

        val timestamp = Utils.getTimeStamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = ""
        hashMap["phonecode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Email"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registeredUserEmail"
        hashMap["uid"] = "$registeredUserUid"

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //firebase db save success
                Log.d(TAG, "updateUserInfo: User registered...")
                progressDialog.dismiss()
                //start main
                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()//finish the current and all activities from back stack
            }
            .addOnFailureListener {e->
                //firebase db save failed
                Log.e(TAG, "updateUserInfo: ", e)
                progressDialog.dismiss()
                Utils.toast(this,"Lưu thông tin không thành công vì ${e.message}")
            }


    }
}