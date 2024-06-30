package com.example.stegnoapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {

    fun getBitmapFromUri(context: Context, uri: Uri?): Bitmap? {
        return uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            BitmapFactory.decodeStream(inputStream)
        }
    }
    fun getUriFromBitmap(bitmap: Bitmap, context: Context,fileName :String): Uri? {
//        val fileName = "temp_image.jpg" // Replace with your desired filename
        val filesDir = context.filesDir // You can use other storage locations if needed

        val file = File(filesDir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // Adjust compression quality as needed
        outputStream.flush()
        outputStream.close()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val authority = context.packageName + ".fileprovider"
            context.grantUriPermission(null, Uri.fromFile(file), Intent.FLAG_GRANT_READ_URI_PERMISSION)
            FileProvider.getUriForFile(context, authority, file)
        } else {
            Uri.fromFile(file)
        }
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): File {
        val cacheDir = context.cacheDir
        val tempFile = File(cacheDir, "$fileName.png")

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fos?.close()
        }
        return tempFile
    }

    fun shareImage(context: Context, bitmap: Bitmap) {
        val fileName = "shared_image"
        val tempFile = saveBitmapToCache(context, bitmap, fileName)
        tempFile.let {
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                it
            )

            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share image via"))
        }
    }

}