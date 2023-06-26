@file:Suppress("DEPRECATION")

package com.snuzj.shoppingapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.snuzj.shoppingapp.databinding.ActivityLoginOptionsBinding

class LoginOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginOptionsBinding

    private companion object{
        private const val TAG ="LOGIN_OPTIONS_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Đang tải")
        progressDialog.setCanceledOnTouchOutside(false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

        binding.closeBtn.setOnClickListener {
            onBackPressed()
        }

        binding.loginEmailBtn.setOnClickListener {
            startActivity(Intent(this, LoginEmailActivity::class.java))
        }

        binding.loginGoogleBtn.setOnClickListener {
            beginGoogleLogin()
        }

        binding.loginPhoneBtn.setOnClickListener {
            startActivity(Intent(this, LoginPhoneActivity::class.java))
        }
    }

    private fun beginGoogleLogin() {
        Log.d(TAG, "beginGoogleLogin: ")
        //intent to launch google sign in options dialog
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL =  registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){result->
        Log.d(TAG, "googleSignInARL: ")

        if(result.resultCode == RESULT_OK){
            //user has chosen same option to login from intent, get data/ intent from the result param to get logged in user info, we will use that to login with firebase auth
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                //get the GoogleSignInAccount from intent
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInARL: Account ID: ${account.id}")
                //sign in success, sign in with firebase auth
                firebaseAuthWithGoogleAccount(account.idToken)
            }
            catch(e: Exception){
                //sign in failed, show exception
                Log.e(TAG, "googleSignInARL: ", e)
                Utils.toast(this,"Đăng nhập thất bại vì ${e.message}")
            }
        }
        else{
            //user has cancelled Google SignIn
            Utils.toast(this,"Hoàn tác thành công")
        }
    }

    private fun firebaseAuthWithGoogleAccount(idToken: String?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken: $idToken")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {authResult->
                if (authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: New User, Account created...")
                    updateUserInfoDb()

                }
                else{
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing User, Logged In...")
                    //existing user, sign in, no need to save user info to db, start Main Activity
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {e->
                Log.e(TAG, "firebaseAuthWithGoogleAccount: ", e)
                Utils.toast(this, "${e.message}")
            }
    }

    private fun updateUserInfoDb() {
        Log.d(TAG, "updateUserInfoDb: ")

        progressDialog.setMessage("Đang lưu thông tin người dùng")
        progressDialog.show()

        val timestamp = Utils.getTimeStamp()
        val registeredUserEmail = firebaseAuth.currentUser?.email
        val registeredUserUid = firebaseAuth.uid
        val name = firebaseAuth.currentUser?.displayName

        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = "$name"
        hashMap["phonecode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Google"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registeredUserEmail"
        hashMap["uid"] = "$registeredUserUid"

        //set data to firebase db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfoDb: User info saved")
                progressDialog.dismiss()

                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Log.e(TAG, "updateUserInfoDb: ", e)
                Utils.toast(this,"Lưu thông tin thất bại vì ${e.message}")
            }
    }

}