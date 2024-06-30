package com.example.stegnoapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

object SteganographyUtils {

    private const val ALGORITHM = "AES"
   private const  val KEY_ALIAS = "YEH_DIL_MANGE_MORE"
//    private const val PARITY_SHARDS = 2
//    private const val DATA_SHARDS = 10
//    private const val TOTAL_SHARDS = DATA_SHARDS + PARITY_SHARDS
    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance(ALGORITHM)
        keyGen.init(256)
        return keyGen.generateKey()
    }
    fun saveKey(context: Context, key: SecretKey) {
        val prefs = context.getSharedPreferences("steganography_prefs", Context.MODE_PRIVATE)
        val keyString = Base64.encodeToString(key.encoded, Base64.DEFAULT)
        prefs.edit().putString(KEY_ALIAS, keyString).apply()
    }

    fun getKey(context: Context): SecretKey? {
        val prefs = context.getSharedPreferences("steganography_prefs", Context.MODE_PRIVATE)
        val keyString = prefs.getString(KEY_ALIAS, null) ?: return null
        val decodedKey = Base64.decode(keyString, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, ALGORITHM)
    }

    @SuppressLint("GetInstance")
    fun encryptMessage(message: String, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedMessage = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
        Log.d("SteganographyUtils", "Encrypted message size: ${encryptedMessage.size}")
        Log.d("SteganographyUtils", "Encrypted message content: ${Base64.encodeToString(encryptedMessage, Base64.DEFAULT)}")
        return encryptedMessage
    }

    @SuppressLint("GetInstance")
    fun decryptMessage(encryptedMessage: ByteArray, key: SecretKey): String {
        Log.d("SteganographyUtils", "Encrypted message to decrypt size: ${encryptedMessage.size}")
        Log.d("SteganographyUtils", "Encrypted message to decrypt content: ${Base64.encodeToString(encryptedMessage, Base64.DEFAULT)}")
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedMessage = cipher.doFinal(encryptedMessage)
        Log.d("SteganographyUtils", "Decrypted message content: ${String(decryptedMessage, Charsets.UTF_8)}")
        return String(decryptedMessage, Charsets.UTF_8)
    }
//    private fun computeCRC(data: ByteArray): ByteArray {
//        val crc32 = java.util.zip.CRC32()
//        crc32.update(data)
//        val crcValue = crc32.value.toInt()
//        return ByteBuffer.allocate(4).putInt(crcValue).array()
//    }
//
//    private fun getRandomSequence(seed: Long, length: Int): IntArray {
//        val random = SecureRandom.getInstance("SHA1PRNG")
//        random.setSeed(seed)
//        val sequence = IntArray(length)
//        for (i in sequence.indices) {
//            sequence[i] = random.nextInt(length)
//        }
//        return sequence
//    }
//fun Int.toByteArray(): ByteArray = byteArrayOf(
//    (this shr 24 and 0xFF).toByte(),
//    (this shr 16 and 0xFF).toByte(),
//    (this shr 8 and 0xFF).toByte(),
//    (this and 0xFF).toByte()
//)



    fun encodeMessage(image: Bitmap, message: String, key: SecretKey): Bitmap {
        val encodedImage = image.copy(Bitmap.Config.ARGB_8888, true)
        val encryptedMessage = encryptMessage(message, key)
        val keyBytes = key.toString().toByteArray(Charsets.UTF_8)
        val messageLength = encryptedMessage.size

        if (messageLength > image.width * image.height / 3 - 4) {
            throw IllegalArgumentException("Message is too long to be hidden in this image")
        }

        val lengthBytes = messageLength.toByteArray()

//        var messageIndex = 0
        var keyIndex = 0

//        fun Byte.toBits(): String = this.toString(2).padStart(8, '0')

        for (i in lengthBytes.indices) {
            val lengthByte = lengthBytes[i]
            for (bitIndex in 0 until 8) {
                val bit = (lengthByte.toInt() shr (7 - bitIndex)) and 1
                val pixel = encodedImage.getPixel(
                    (i * 8 + bitIndex) % encodedImage.width,
                    (i * 8 + bitIndex) / encodedImage.width
                )
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val modifiedB = (b and 0xFE) or bit
                val newPixel = (r shl 16) or (g shl 8) or modifiedB
                encodedImage.setPixel(
                    (i * 8 + bitIndex) % encodedImage.width,
                    (i * 8 + bitIndex) / encodedImage.width,
                    newPixel
                )
            }
        }

        for (i in encryptedMessage.indices) {
            val messageByte = encryptedMessage[i] xor keyBytes[keyIndex]
            for (bitIndex in 0 until 8) {
                val bit = (messageByte.toInt() shr (7 - bitIndex)) and 1
                val pixel = encodedImage.getPixel(
                    (32 + i * 8 + bitIndex) % encodedImage.width,
                    (32 + i * 8 + bitIndex) / encodedImage.width
                )
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val modifiedB = (b and 0xFE) or bit
                val newPixel = (r shl 16) or (g shl 8) or modifiedB
                encodedImage.setPixel(
                    (32 + i * 8 + bitIndex) % encodedImage.width,
                    (32 + i * 8 + bitIndex) / encodedImage.width,
                    newPixel
                )
            }
            keyIndex = (keyIndex + 1) % keyBytes.size
        }

        return encodedImage
    }

    private fun Int.toByteArray(): ByteArray = byteArrayOf(
        (this shr 24 and 0xFF).toByte(),
        (this shr 16 and 0xFF).toByte(),
        (this shr 8 and 0xFF).toByte(),
        (this and 0xFF).toByte()
    )


    fun decodeMessage(image: Bitmap, key: SecretKey): String {
        val keyBytes = key.toString().toByteArray(Charsets.UTF_8)
        var keyIndex = 0

        var length = 0
        for (i in 0 until 32 step 8) {
            for (bitIndex in 0 until 8) {
                val pixel = image.getPixel((i + bitIndex) % image.width, (i + bitIndex) / image.width)
                val b = pixel and 0xFF
                val bit = b and 1
                length = length or (bit shl (31 - (i + bitIndex)))
            }
        }

        val messageBytes = ByteArray(length)

        for (i in 0 until length) {
            var messageByte = 0
            for (bitIndex in 0 until 8) {
                val pixel = image.getPixel((32 + i * 8 + bitIndex) % image.width, (32 + i * 8 + bitIndex) / image.width)
                val b = pixel and 0xFF
                val bit = b and 1
                messageByte = messageByte or (bit shl (7 - bitIndex))
            }
            messageBytes[i] = (messageByte xor keyBytes[keyIndex].toInt()).toByte()
            keyIndex = (keyIndex + 1) % keyBytes.size
        }
//        String(messageBytes, Charsets.UTF_8)
        return decryptMessage(messageBytes,key)
    }


}