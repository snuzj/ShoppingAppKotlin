package com.snuzj.shoppingapp

import android.net.Uri

class ModelImagePicked  {
    var id = ""
    var imageUri: Uri? = null
    var imageUrl: String? = null
    var fromInternet = false

    constructor()

    constructor(id: String, imageUri: Uri, imageUrl: String?, fromInternet: Boolean){
        this.id = id
        this.imageUrl = imageUrl
        this.imageUri = imageUri
        this.fromInternet = fromInternet
    }
}