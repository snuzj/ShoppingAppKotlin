@file:Suppress("DEPRECATION")

package com.snuzj.shoppingapp.activities

import android.app.Activity
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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.snuzj.shoppingapp.AdapterImagePicked
import com.snuzj.shoppingapp.ModelImagePicked
import com.snuzj.shoppingapp.R
import com.snuzj.shoppingapp.Utils
import com.snuzj.shoppingapp.databinding.ActivityAdCreateBinding

class AdCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdCreateBinding

    private companion object{
        private const val TAG = "AD_CREATE_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    //image Uri to hold url of the image (pick/capture of using Camera/Gallery) to add to image list
    private var imageUri: Uri? = null

    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>

    private lateinit var adapterImagePicked: AdapterImagePicked

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Đang tải...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.postAdBtn.setOnClickListener {
            validateData()
        }

        // Set up and set adapter to the category input field i.e categoryAct
        val adapterCategories = ArrayAdapter(this, R.layout.row_category_act, Utils.categories)
        binding.categoryAct.setAdapter(adapterCategories)

        // Set item click listener for the categoryAct dropdown list
        binding.categoryAct.setOnItemClickListener { _, _, position, _ ->

            // Perform any desired action when a category is selected
            // For example, you can add a new list based on the selected category
            when (adapterCategories.getItem(position)) {
                "Bất động sản" -> {
                    binding.newListTil.visibility = View.VISIBLE
                    val newList = ArrayAdapter(this, R.layout.row_new_list, Utils.realEstate)
                    binding.newListAct.setAdapter(newList)
                }
                "Xe cộ" -> {
                    binding.newListTil.visibility = View.VISIBLE
                    val newList = ArrayAdapter(this, R.layout.row_new_list, Utils.vihicles)
                    binding.newListAct.setAdapter(newList)
                }
                "Đồ điện tử" -> {
                    binding.newListTil.visibility = View.VISIBLE
                    val newList = ArrayAdapter(this, R.layout.row_new_list, Utils.electronicDevices)
                    binding.newListAct.setAdapter(newList)
                }
                "Thú cưng" -> {
                    binding.newListTil.visibility = View.VISIBLE
                    val newList = ArrayAdapter(this, R.layout.row_new_list, Utils.pets)
                    binding.newListAct.setAdapter(newList)
                }
                else -> {
                    binding.newListTil.visibility = View.GONE
                }
            }
        }

        //show and set the conditions adapter to the Condition Input Filled: conditionAct
        val adapterCondition = ArrayAdapter(this,R.layout.row_condition_act,Utils.conditions)
        binding.conditionAct.setAdapter(adapterCondition)

        //init imagePickedArrayList
        imagePickedArrayList = ArrayList()

        //load images
        loadImages()

        binding.addImageBtn.setOnClickListener {
            showImagePickOptions()
        }


    }

    private fun loadImages() {
        Log.d(TAG, "loadImages: ")
        adapterImagePicked = AdapterImagePicked(this,imagePickedArrayList)

        binding.imagesRv.adapter = adapterImagePicked
    }

    private fun showImagePickOptions() {
        Log.d(TAG, "showImagePickOptions: ")
        //init the PopupMenu(param 1: context,param2: achor view for this popup)
        val popupMenu = PopupMenu(this, binding.addImageBtn)
        //add menu items
        popupMenu.menu.add(Menu.NONE, 1,1,"Camera")
        popupMenu.menu.add(Menu.NONE, 2,2,"Thư viện")
        //show popup menu
        popupMenu.show()
        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener { item->
            //get the id of the item clicked in popup menu
            val itemId = item.itemId
            //check which item id is clicked as defined
            if (itemId == 1){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

                    val cameraPermissions = arrayOf(android.Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermissions)
                }
                else{
                    val cameraPermissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermission.launch(cameraPermissions)
                }
            } else if(itemId == 2){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                }
                else{
                    val storagePermissions = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermissions)
                }
            }
            true

        }

    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if(isGranted){
            //storage permission granted
            pickImageGallery()
        } else{
            //storage permission denied
            Utils.toast(this,"Quyền truy cập bị từ chối")
        }

    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){result->
        Log.d(TAG, "requestCameraPermission: isGranted: $result")

        var areAllGranted = true
        for(isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if(areAllGranted){
            //all permissions camera, storage are granted, we can now launch camera to capture image
            pickImageCamera()
        }
        else{
            //camera or storage both denied
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
            Log.d(TAG, "galleryActivityResultLauncher: ")
            //check if image is picked or not
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                Log.d(TAG, "galleryActivityResultLauncher: imageUri: $imageUri")

                val timestamp = "${Utils.getTimeStamp()}"

                val modelImagePicked = imageUri?.let { ModelImagePicked(timestamp, it,null,false) }

                if (modelImagePicked != null) {
                    imagePickedArrayList.add(modelImagePicked)
                }

                loadImages()
            } else {
                Utils.toast(this, "Hoàn tác...")
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
                //timestamp will be used as id the image picked
                val timestamp = "${Utils.getTimeStamp()}"
                //setup model for image
                val modelImagePicked = imageUri?.let { ModelImagePicked(timestamp, it,null,false) }

                if (modelImagePicked != null) {
                    imagePickedArrayList.add(modelImagePicked)
                } //add model to array list
                loadImages() //reload images
            } else{
                //cancelled
                Utils.toast(this,"Đã hoàn tác")
            }
        }

    private var brand = ""
    private var category = ""
    private var condition = ""
    private var address = ""
    private var price = ""
    private var title = ""
    private var description = ""
    private var latitude = 0.0
    private var longtitude = 0.0

    private fun validateData(){
        Log.d(TAG, "validateData: ")

        //get data
        brand = binding.brandEt.text.toString().trim()
        category = binding.newListAct.text.toString().trim()
        condition = binding.conditionAct.text.toString().trim()
        address = binding.locationAct.text.toString().trim()
        price = binding.priceEt.text.toString().trim()
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.toString().trim()

        //validate data
        when {
            brand.isEmpty() -> {
                //no brand entered in brandEt, show error
                binding.brandEt.error = "Nhập tên thương hiệu"
                binding.brandEt.requestFocus()
            }
            category.isEmpty() -> {
                //no categoryAct entered, show error
                binding.categoryAct.error = "Chọn danh mục"
                binding.categoryAct.requestFocus()
            }
            condition.isEmpty() -> {
                //no condition entered in conditionAct, show error
                binding.conditionAct.error = "Chọn tình trạng"
                binding.conditionAct.requestFocus()
            }
            title.isEmpty() -> {
                //no title entered in titleEt, show error
                binding.titleEt.error = "Nhập tiêu đề"
                binding.titleEt.requestFocus()
            }
            description.isEmpty() -> {
                //no description entered in descriptionEt, show error
                binding.descriptionEt.error = "Nhập nội dung bài đăng"
                binding.descriptionEt.requestFocus()
            }
            else -> {
                postAd()
            }
        }

    }

    private fun postAd() {
        Log.d(TAG, "postAd: Publishing Ad")

        //show progress
        progressDialog.setMessage("Đang đăng bài")
        progressDialog.show()

        //get current timestamp
        val timestamp = Utils.getTimeStamp()

        //firebase database Ads ref to store new Ads
        val refAds = FirebaseDatabase.getInstance().getReference("Ads")

        //keyId from ref to use as Ad id
        val keyId = refAds.push().key

        //set up data to add in firebase database
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$keyId"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["brand"] = brand
        hashMap["category"] = category
        hashMap["condition"] = condition
        hashMap["address"] = address
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["status"] = Utils.AD_STATUS_AVAILABLE
        hashMap["timestamp"] = timestamp
        hashMap["latitude"] = latitude
        hashMap["longtitude"] = longtitude

        //set data to firebase database: Ads -> AdId -> AdDataJson
        refAds.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "postAd: Ad Published")
                uploadImageStorage(keyId)
            }
            .addOnFailureListener{e->
                Log.e(TAG, "postAd: ", e)
                progressDialog.dismiss()
                Utils.toast(this,"Thao tác thất bại. ${e.message}")
            }
    }

    private fun uploadImageStorage(adId: String) {
        for (i in imagePickedArrayList.indices){

            val modelImagePicked = imagePickedArrayList[i]

            val imageName = modelImagePicked.id

            val filePathAndName = "Ads/$imageName"
            val imageIndexForProgress = i + 1


            val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
            storageReference.putFile(modelImagePicked.imageUri!!)
                .addOnProgressListener {snapshot->
                    //calculate the current progress of uploading image
                    val progress = 100.0 + snapshot.bytesTransferred / snapshot.totalByteCount
                    Log.d(TAG, "uploadImageStorage: progress $progress")
                    val message = "Đang tải $imageIndexForProgress of ${imagePickedArrayList.size} ... Tiến trình ${progress.toInt()}"

                    Log.d(TAG, "uploadImageStorage: message: $message")

                    progressDialog.setMessage(message)
                    progressDialog.show()

                }
                .addOnSuccessListener {taskSnapshot->
                    //image uploaded
                    Log.d(TAG, "uploadImageStorage: onSuccess")
                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val uploadedImageUrl = uriTask.result

                    if (uriTask.isSuccessful){

                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = "${modelImagePicked.imageUri}"
                        hashMap["imageUrl"] = "$uploadedImageUrl"

                        val ref = FirebaseDatabase.getInstance().getReference("Ads")
                        ref.child(adId).child("Images")
                            .child(imageName)
                            .updateChildren(hashMap)
                    }

                    progressDialog.dismiss()

                }
                .addOnFailureListener{e->
                    //failed to upload image
                    Log.e(TAG, "uploadImageStorage: ", e)
                    progressDialog.dismiss()

                }
        }
    }
}
