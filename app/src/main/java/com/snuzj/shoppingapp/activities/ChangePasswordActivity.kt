@file:Suppress("DEPRECATION")

package com.snuzj.shoppingapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.snuzj.shoppingapp.Utils
import com.snuzj.shoppingapp.databinding.ActivityChangePasswordBinding


class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private companion object{
        private const val TAG = "CHANGE_PASSWORD_TAG"
    }
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!


        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Đang tải...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private var currentPassword = ""
    private var newPassword = ""
    private var confirmNewPassword = ""

    private fun validateData() {
        //get data
        currentPassword = binding.currentPasswordEt.text.toString().trim()
        newPassword = binding.newPasswordEt.text.toString().trim()
        confirmNewPassword = binding.confirmPasswordEt.text.toString().trim()

        Log.d(TAG, "validateData: currentPassword: $currentPassword")
        Log.d(TAG, "validateData: newPassword: $newPassword")
        Log.d(TAG, "validateData: confirmNewPassword: $confirmNewPassword")

        //validate data
        when {
            currentPassword.isEmpty() -> {
                binding.currentPasswordEt.error = "Hãy nhập mật khẩu hiện tại của bạn"
                binding.currentPasswordEt.requestFocus()
            }
            newPassword.isEmpty() -> {
                binding.newPasswordEt.error = "Hãy nhập mật khẩu mới"
                binding.newPasswordEt.requestFocus()
            }
            confirmNewPassword != newPassword -> {
                binding.confirmPasswordEt.error = "Mật khẩu xác nhận không khớp"
                binding.confirmPasswordEt.requestFocus()
            }
            else -> {
                authenticateUserForUpdatePassword()
            }
        }
    }

    private fun authenticateUserForUpdatePassword() {
        //show progress
        progressDialog.setMessage("Đang xác thực người dùng")
        progressDialog.show()

        val authCredential = EmailAuthProvider.getCredential(firebaseUser.email.toString(),currentPassword)
        firebaseUser.reauthenticate(authCredential)
            .addOnSuccessListener {
                Log.d(TAG, "authenticateUserForUpdatePassword: success")
                updatePassword()

            }
            .addOnFailureListener { e->
                Log.e(TAG, "authenticateUserForUpdatePassword: ", e)
                progressDialog.dismiss()
                Snackbar.make(binding.root, "Xác thực thất bại vì ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun updatePassword() {
        //show progress
        progressDialog.setMessage("Đang đổi mật khẩu")
        progressDialog.show()

        firebaseUser.updatePassword(newPassword)
            .addOnSuccessListener {
                Log.d(TAG, "updatePassword: success")
                progressDialog.dismiss()
                Utils.toast(this, "Thay đổi mật khẩu thành công.")

                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                Log.e(TAG, "updatePassword: ", e)
                Snackbar.make(binding.root, "Cập nhật thất bại vì ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }


}