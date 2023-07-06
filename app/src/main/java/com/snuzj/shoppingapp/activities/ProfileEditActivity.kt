@file:Suppress("DEPRECATION")

package com.snuzj.shoppingapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.snuzj.shoppingapp.R
import com.snuzj.shoppingapp.Utils
import com.snuzj.shoppingapp.databinding.ActivityProfileEditBinding
import java.util.Calendar

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    private companion object{
        private const val TAG = "PROFILE_EDIT_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var firebaseUser: FirebaseUser? = null

    private var myUserType = ""

    private var imageUri: Uri? = null

    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Đang tải...")
        progressDialog.setCanceledOnTouchOutside(false)
        
        firebaseAuth = FirebaseAuth.getInstance()
        loadMyInfo()

        firebaseUser = firebaseAuth.currentUser


        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.profileImagePickFab.setOnClickListener {
            imagePickDialog()
        }

        binding.dobEt.setOnClickListener {
            val currentDate = Calendar.getInstance()
            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH)
            val day = currentDate.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                selectedDate = formattedDate
                binding.dobEt.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }

        binding.deleteAccountCv.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage("Bạn chắc chắn muốn xóa tài khoản này không?")
            alertDialogBuilder.setPositiveButton("Có") { dialog, _ ->
                deleteAccount()
                dialog.dismiss()
            }
            alertDialogBuilder.setNegativeButton("Không") { dialog, _ ->
                dialog.dismiss()
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

    }

    //1.Delete user account from account in Firebase Authentication
    private fun deleteAccount() {
        // Xóa tài khoản người dùng
        Log.d(TAG, "deleteAccount: ")
        progressDialog.setMessage("Đang xóa tài khoản của bạn")
        progressDialog.show()

        firebaseUser!!.delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteAccount: deleting...")

                val myUid = firebaseAuth.uid

                progressDialog.setMessage("Đang xóa thông tin liên quan")

                // 2. Xóa quảng cáo của người dùng
                val refUserAds = FirebaseDatabase.getInstance().getReference("Ads")
                refUserAds.orderByChild("uid").equalTo(myUid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                ds.ref.removeValue()
                            }
                            // 3. Xóa dữ liệu người dùng
                            progressDialog.setMessage("Đang xóa dữ liệu người dùng")
                            val refUsers = FirebaseDatabase.getInstance().getReference("Users")
                            refUsers.child(myUid!!)
                                .removeValue()
                                .addOnSuccessListener {
                                    Log.d(TAG, "onDataChange: deleted successfully")
                                    progressDialog.dismiss()
                                    startMainActivity()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "onDataChange: ", e)
                                    progressDialog.dismiss()
                                    Utils.toast(this@ProfileEditActivity, "Hãy đăng nhập lại để xóa tài khoản")
                                    startMainActivity()
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Xử lý lỗi khi huỷ bỏ thao tác
                        }
                    })
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "deleteAccount: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Hãy đăng nhập lại để xóa tài khoản")
            }
    }


    private fun startMainActivity() {
        startActivity(Intent(this,MainActivity::class.java))
        finishAffinity()
    }



    private var name = ""
    private var dob = ""
    private var email = ""
    private var phoneCode = ""
    private var phoneNumber = ""


    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        dob = binding.dobEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        phoneCode = binding.phoneCodeTil.selectedCountryCodeWithPlus
        phoneNumber = binding.phoneNumberEt.text.toString().trim()

        if(imageUri == null){
            updateProfile(null)
        } else {
            uploadProfileImageStorage()
        }

    }

    private fun uploadProfileImageStorage() {
        Log.d(TAG, "uploadProfileImageStorage: ")
        //show progress
        progressDialog.setMessage("Đang tải ảnh lên")
        progressDialog.show()

        val filePathAndName = "UserProfile/profile_${firebaseAuth.uid}"

        val ref = FirebaseStorage.getInstance().getReference(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnProgressListener {snapshot->
                //check image upload progress and show
                val progress = 100.0*snapshot.bytesTransferred / snapshot.totalByteCount
                Log.d(TAG, "uploadProfileImageStorage: progress $progress")
                progressDialog.setMessage("Đang tải ảnh lên. Tiến trình $progress")
            }
            .addOnSuccessListener {taskSnapshot->
                //image uploaded successfully
                Log.d(TAG, "uploadProfileImageStorage: Image Uploaded...")
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                val uploadedImageUrl = uriTask.result.toString()
                if(uriTask.isSuccessful){
                    updateProfile(uploadedImageUrl)
                }
            }
            .addOnFailureListener {e->
                //failed to upload image
                Log.d(TAG, "uploadProfileImageStorage: ",e)
                progressDialog.dismiss()
                Utils.toast(this,"Tải ảnh thất bại vì ${e.message}")
            }
    }

    private fun updateProfile(uploadedImageUrl: String?) {
        Log.d(TAG, "updateProfile: uploadedImageUrl: $uploadedImageUrl")
        progressDialog.setMessage("Đang cập nhật thông tin người dùng")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = name
        hashMap["dob"] = dob
        if (uploadedImageUrl != null){
            //update when image url is not null
            hashMap["profileImageUrl"] = uploadedImageUrl

        }
        else if (myUserType.equals("Phone", true)){
            //if userType is Phone, allow change Email
            hashMap["email"] = email
        }
        else if(myUserType.equals("Email",true) || myUserType.equals("Google",true)){
            //if userType is Email or Google, allow change Phone number
            hashMap["phoneCode"] = phoneCode
            hashMap["phoneNumber"] = phoneNumber
        }

        //db ref of user to update info
        val refercence = FirebaseDatabase.getInstance().getReference("Users")
        refercence.child("${firebaseAuth.uid}")
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateProfile: updated")
                progressDialog.dismiss()
                Utils.toast(this,"Cập nhật thành công")
                imageUri = null
            }
            .addOnFailureListener {e->
                Log.e(TAG, "updateProfile:",e)
                progressDialog.dismiss()
                Utils.toast(this,"Cập nhật thông tin người dùng thất bại vì ${e.message}")
            }
    }

    private fun loadMyInfo() {
        Log.d(TAG, "loadMyInfo: ")
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
                    myUserType = "${snapshot.child("userType").value}"

                    val phone = phoneCode + phoneNumber

                    //if sign in by email, google or phone
                    if(myUserType.equals("Email",true) || myUserType.equals("Google",true)){
                        binding.emailTil.isEnabled = false
                        binding.emailEt.isEnabled = false
                    }
                    else{
                        binding.phoneNumberTil.isEnabled = false
                        binding.phoneNumberEt.isEnabled = false
                        binding.phoneCodeTil.isEnabled = false
                    }

                    //set user info
                    binding.emailEt.setText(email)
                    binding.dobEt.setText(dob)
                    binding.nameEt.setText(name)
                    binding.phoneNumberEt.setText(phoneNumber)

                    //set phone code picker
                    try{
                        val phoneCodeInt = phoneCode.replace("+","").toInt()
                        binding.phoneCodeTil.setCountryForPhoneCode(phoneCodeInt)
                    } catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }

                    //set default profile image
                    try{
                        Glide.with(this@ProfileEditActivity)
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

    private fun imagePickDialog(){
        val popupMenu = PopupMenu(this, binding.profileImagePickFab)

        popupMenu.menu.add(Menu.NONE,1,1,"Camera")
        popupMenu.menu.add(Menu.NONE,2,2,"Thư viện")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item->
            val itemId = item.itemId

            if(itemId == 1){
                Log.d(TAG, "imagePickDialog: Camera Clicked, check if camera permission granted or not")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    requestCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA))
                }
                else{
                    requestCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }
            else if (itemId == 2){
                Log.d(TAG, "imagePickDialog: Gallery Clicked, check if storage permission granted or not")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                }
                else{
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

            return@setOnMenuItemClickListener true
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){result->
            Log.d(TAG, "requestCameraPermissions: result: $result")

            //lets check if permissions are granted or not
            var areAllGranted = true
            for (isGranted in result.values){
                areAllGranted = areAllGranted && isGranted
            }

            if  (areAllGranted){
                Log.d(TAG, "requestCameraPermissions: All granted")
                pickImageCamera()
            } else {
                Log.d(TAG, "requestCameraPermissions: All or either one is denied")
                Utils.toast(this,"Quyền truy cập bị từ chối")
            }
        }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
            Log.d(TAG, "requestStoragePermission: isGranted $isGranted")

            if(isGranted){
                pickImageGallery()
            } else{
                Utils.toast(this,"Quyền truy cập bị từ chối")
            }
        }

    private fun pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"

        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                try{
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.round_person_orange)
                        .into(binding.profileIv)
                } catch (e: Exception){
                    Log.e(TAG, "storageActivityResultLauncher: ", e)
                }
            }
        }

    private fun pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image_description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            //check if image is captured or not
            if(result.resultCode == Activity.RESULT_OK){
                Log.d(TAG, "cameraActivityResultLauncher: Image Captured: ImageUri: $imageUri")
                //set to profileIv
                try{
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.round_person_orange)
                        .into(binding.profileIv)
                } catch (e: Exception){
                    Log.e(TAG, "cameraActivityResultLauncher: ", e)
                }
            } else{
                //cancelled
                Utils.toast(this,"Đã hoàn tác")
            }
        }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        startMainActivity()
    }
}