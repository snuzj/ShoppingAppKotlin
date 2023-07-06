package com.snuzj.shoppingapp.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.snuzj.shoppingapp.fragments.AccountFragment
import com.snuzj.shoppingapp.fragments.ChatsFragment
import com.snuzj.shoppingapp.fragments.HomeFragment
import com.snuzj.shoppingapp.R
import com.snuzj.shoppingapp.Utils
import com.snuzj.shoppingapp.databinding.ActivityMainBinding
import com.snuzj.shoppingapp.fragments.MyAdsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if(auth.currentUser == null){
            startLoginOptions()
        }

        binding.bottomNv.setOnItemSelectedListener{item->
            when(item.itemId){
                R.id.menu_home ->{
                    showHomeFragment()
                    true
                }
                R.id.menu_chats ->{
                    if(auth.currentUser == null){
                        Utils.toast(this, "Yêu cầu đăng nhập")
                        startLoginOptions()
                        false
                    }
                    else{
                        showChatsFragment()
                        true
                    }

                }
                R.id.menu_sell ->{
                    true
                }
                R.id.menu_my_ads ->{
                    if(auth.currentUser == null){
                        Utils.toast(this, "Yêu cầu đăng nhập")
                        startLoginOptions()
                        false
                    }
                    else{
                        showMyAdsFragment()
                        true
                    }

                }
                R.id.menu_account ->{
                    if(auth.currentUser == null){
                        Utils.toast(this, "Yêu cầu đăng nhập")
                        startLoginOptions()
                        false
                    }
                    else{
                        showAccountFragment()
                        true
                    }

                }
                else->{
                    false
                }

            }
        }

        binding.sellFab.setOnClickListener {
            startActivity(Intent(this,AdCreateActivity::class.java))
        }
    }

    private fun startLoginOptions() {
        startActivity(Intent(this, LoginOptionsActivity::class.java))
    }

    private fun showHomeFragment(){
        binding.toolbarTitleTv.text = "Trang chủ"

        //show HomeFragment
        val fragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "HomeFragment")
            .commit()

    }

    private fun showChatsFragment(){
        binding.toolbarTitleTv.text = "Trò chuyện"

        //show ChatsFragment
        val fragment = ChatsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "ChatsFragment")
            .commit()
    }

    private fun showMyAdsFragment(){
        binding.toolbarTitleTv.text = "Quảng cáo của tôi"

        //show MyAdsFragment
        val fragment = MyAdsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "MyAdsFragment")
            .commit()
    }

    private fun showAccountFragment(){
        binding.toolbarTitleTv.text = "Tài khoản của tôi"

        //show MyAdsFragment
        val fragment = AccountFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "AccountFragment")
            .commit()
    }

}