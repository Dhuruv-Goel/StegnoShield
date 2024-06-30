package com.example.stegnoapp.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.stegnoapp.databinding.ActivityEncodeBinding
import com.example.stegnoapp.utils.ImageUtils
import com.example.stegnoapp.utils.SteganographyUtils
import java.io.IOException
import java.io.OutputStream

class EncodeActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityEncodeBinding.inflate(layoutInflater)
    }
    private var encodedImage: Bitmap? = null
    private var selectedImage: Bitmap? = null

    //    private var imageCount :Int = 1
    private var fileName: String? = null
    private lateinit var imageStartActivityForResult: ActivityResultLauncher<Intent>
    private lateinit var savedImageStartActivityForResult: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        imageCount = 1
        fileName = null
        imageStartActivityForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result?.data?.data?.let {
                        selectedImage = ImageUtils.getBitmapFromUri(this, it)
                        binding.imageView.setImageBitmap(selectedImage)
                    }
                }
            }
        savedImageStartActivityForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result?.data?.data?.let { uri ->
                        if (encodedImage == null) {
                            showToast("First Encode the Image")
                        }

                        encodedImage?.let {
                            fileName?.let { it1 ->
                                saveBitmapToSelectedDirectory(
                                    this,
                                    it, uri, it1
                                )
//                                imageCount++
                            }
                        }

                    }
                }
            }
        binding.apply {
            btnSelectImage.setOnClickListener {
                selectedImageFromGallery()
            }
            btnEncode.setOnClickListener {
                encodeMessage()
            }
            btnSave.setOnClickListener {
                fileName = binding.etFileName.text.toString()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

                if (fileName != null) {
                    savedImageStartActivityForResult.launch(intent)
                } else {
                    etFileName.error = "Mention the fileName."
                }
            }
            btnShare.setOnClickListener {
                encodedImage?.let { it1 -> ImageUtils.shareImage(this@EncodeActivity, it1) }
//                val imageUri =
//                    encodedImage?.let { it1 ->
//                        fileName?.let { it2 ->
//                            ImageUtils.getUriFromBitmap(
//                                it1, this@EncodeActivity,
//                                "$it2.jpg"
//                            )
//                        }
//                    }
//                val shareIntent = Intent(Intent.ACTION_SEND).apply {
//                    // Set the intent type to image
//                    type = "image/*"
//                    // Add the image URI as an extra
//                    putExtra(Intent.EXTRA_STREAM, imageUri)
//                    // Set flags for temporary permission (recommended)
//                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                }
//                startActivity(Intent.createChooser(shareIntent, "Share image to app:"))
            }
        }
    }

    private fun selectedImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imageStartActivityForResult.launch(intent)
    }

    private fun encodeMessage() {
        val message = binding.etMessage.text.toString()
        val secretKey = SteganographyUtils.getKey(this)!!
        Log.d("sk", secretKey.toString())
        selectedImage?.let { bitmap ->
            encodedImage = SteganographyUtils.encodeMessage(bitmap, message, secretKey)
            binding.imageView.setImageBitmap(encodedImage)
            Toast.makeText(this, "Message encoded", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun saveImage(){
//        if(encodedImage==null){
//            return showToast("First Encode the Image")
//        }
//        imageCount = 1
//        val fileName = "encoded_image_$imageCount"
//        val filePath = encodedImage?.let { LocalStorageUtils.saveBitmapToInternalStorage(this, it,fileName) }
//
//        imageCount++
//        if (filePath != null) {
//            showToast("Encoded image saved at: $filePath")
//        } else {
//            showToast("Failed to save encoded image.")
//        }
//    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun saveBitmapToSelectedDirectory(
        context: Context,
        bitmap: Bitmap,
        uri: Uri,
        fileName: String
    ) {
        val documentFile = DocumentFile.fromTreeUri(context, uri)
        documentFile?.let {
            val file = it.createFile("image/png", fileName)
            context.contentResolver.openOutputStream(file?.uri ?: return).use { outputStream ->
                saveBitmapToStream(bitmap, outputStream)
            }
        }
    }

    private fun saveBitmapToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        outputStream?.let {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                it.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    it.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}