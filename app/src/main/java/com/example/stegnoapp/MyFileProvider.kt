package com.example.stegnoapp

import android.content.Context
import androidx.core.content.FileProvider

class MyFileProvider : FileProvider() {
    // Specify the authorities here based on your package name
    fun getAuthorities(context: Context): String {
        return "com.example.stegnoapp.fileprovider"
    }
}