package com.snuzj.shoppingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.snuzj.shoppingapp.databinding.ActivityMainBinding

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
                R.id.menu_home->{
                    showHomeFragment()
                    true
                }
                R.id.menu_chats->{
                    showChatsFragment()
                    true
                }
                R.id.menu_sell->{
                    true
                }
                R.id.menu_my_ads->{
                    showMyAdsFragment()
                    true
                }
                R.id.menu_account->{
                    showAccountFragment()
                    true
                }
                else->{
                    false
                }

            }
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
        val fragment = ChatsFragment()
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