package com.example.stegnoapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.stegnoapp.databinding.ActivityMainBinding
import com.example.stegnoapp.utils.SteganographyUtils
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var secretKey: SecretKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // Generate and save the key if it doesn't already exist
        secretKey = SteganographyUtils.getKey(this) ?: run {
            val generatedKey = SteganographyUtils.generateKey()
            SteganographyUtils.saveKey(this, generatedKey)
            generatedKey
        }
        Log.d("sk",secretKey.toString())

        binding.apply {
            btnencode.setOnClickListener {
                val intent = Intent(this@MainActivity, EncodeActivity::class.java)
                startActivity(intent)
            }
            btndecode.setOnClickListener {
                val intent = Intent(this@MainActivity, DecodeActivity::class.java)
                startActivity(intent)
            }
        }
    }
}