package com.snuzj.shoppingapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.snuzj.shoppingapp.R
import com.snuzj.shoppingapp.Utils
import com.snuzj.shoppingapp.activities.MainActivity
import com.snuzj.shoppingapp.activities.ProfileEditActivity
import com.snuzj.shoppingapp.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private companion object {
        private const val TAG = "ACCOUNT_TAG"
    }

    private lateinit var binding: FragmentAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        loadMyInfo()

        binding.logoutCv.setOnClickListener {
            firebaseAuth.signOut()//logout user
            startActivity(Intent(mContext, MainActivity::class.java)) //start mainActivity
            activity?.finishAffinity()
        }

        binding.editProfileCv.setOnClickListener{
            startActivity(Intent(mContext,ProfileEditActivity::class.java))
        }
    }

    private fun loadMyInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child("${firebaseAuth.uid}")
                .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val name = "${snapshot.child("name").value}"
                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //make full phone number
                    val phone = phoneCode + phoneNumber

                    //to avoid null or format exceptions
                    if(timestamp == "null"){
                        timestamp = "0"
                    }

                    //format timestamp dd/MM/yyyy
                    val formattedDate = Utils.formatTimestampDate(timestamp.toLong())

                    //set data to UI
                    binding.emailTv.text = email
                    binding.nameTv.text = name
                    binding.dobTv.text = dob
                    binding.phoneTv.text = phone
                    binding.memberSinceTv.text = formattedDate

                    //check user is verified or not
                    if(userType == "Email"){
                        val isVerified = firebaseAuth.currentUser?.isEmailVerified
                        if (isVerified == true){
                            binding.verificationTv.text = "Đã xác minh"
                        }
                        else{
                            binding.verificationTv.text = "Chưa xác minh"
                        }
                    }
                    else{
                        binding.verificationTv.text = "Đã xác minh"
                    }

                    //set default profile image
                    try{
                        Glide.with(mContext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.round_person_orange)
                            .into(binding.profileIv)

                    } catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }



                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

}