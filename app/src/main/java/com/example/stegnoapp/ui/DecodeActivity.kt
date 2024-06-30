package com.example.stegnoapp.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.stegnoapp.databinding.ActivityDecodeBinding
import com.example.stegnoapp.utils.ImageUtils
import com.example.stegnoapp.utils.SteganographyUtils

class DecodeActivity:AppCompatActivity() {
    private val binding by lazy {
        ActivityDecodeBinding.inflate(layoutInflater)
    }
    private var selectedImage: Bitmap? = null
    private lateinit var imageActivityForResult :ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        imageActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result?.data?.data?.let{
                    selectedImage = ImageUtils.getBitmapFromUri(this, it)
                    binding.imageView.setImageBitmap(selectedImage)
                }
            }
        }
        binding.apply{
            btnDecode.setOnClickListener {
                decodeMessage()
            }
            btnSelectImage.setOnClickListener {
                selectImageFromGallery()
            }
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
       imageActivityForResult.launch(intent)
    }

    private fun decodeMessage() {
        val secretKey = SteganographyUtils.getKey(this)!!
        Log.d("sk",secretKey.toString())
        selectedImage?.let { bitmap ->
            val message = SteganographyUtils.decodeMessage(bitmap, secretKey)
            binding.tvMessage.text = message
        } ?: run {
            Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show()
        }
    }
}