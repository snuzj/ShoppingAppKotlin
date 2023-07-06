package com.snuzj.shoppingapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.snuzj.shoppingapp.databinding.RowImagesPickedBinding

class AdapterImagePicked(
    private val context: Context,
    private val imagesPickedArrayList: ArrayList<ModelImagePicked>
) : RecyclerView.Adapter<AdapterImagePicked.HolderImagePicked>() {
    private lateinit var binding : RowImagesPickedBinding

    private companion object {
        private const val TAG = "IMAGE_TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagePicked {
        binding = RowImagesPickedBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderImagePicked(binding.root)
    }

    override fun getItemCount(): Int {
        return imagesPickedArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {
        val model = imagesPickedArrayList[position]
        val imageUri = model.imageUri
        Log.d(TAG, "onBindViewHolder: imageUri $imageUri")

        try{
            Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.round_add_a_photo)
                .into(holder.imageIv)
        } catch (e: Exception){
            Log.e(TAG, "onBindViewHolder: ", e)
        }
        //handle click, remove image
        holder.closeBtn.setOnClickListener {
            imagesPickedArrayList.remove(model)
            notifyDataSetChanged()
        }
    }

    inner class HolderImagePicked(itemView: View) : ViewHolder(itemView){
        var imageIv = binding.imageIv
        var closeBtn = binding.closeBtn
    }




}