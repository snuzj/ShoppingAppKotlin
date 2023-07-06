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
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
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

                    val cameraPermissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermission.launch(cameraPermissions)
                }
                else{
                    val cameraPermissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermission.launch(cameraPermissions)
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

                val modelImagePicked = ModelImagePicked(timestamp,imageUri,null,false)

                imagePickedArrayList.add(modelImagePicked)

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
                val modelImagePicked = ModelImagePicked(timestamp,imageUri,null,false)

                imagePickedArrayList.add(modelImagePicked) //add model to array list
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

    }
}