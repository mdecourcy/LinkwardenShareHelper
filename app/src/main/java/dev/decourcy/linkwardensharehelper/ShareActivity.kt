package dev.decourcy.linkwardensharehelper

import android.content.Intent
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.decourcy.linkwardensharehelper.data.api.LinkwardenApi
import dev.decourcy.linkwardensharehelper.data.model.LinkwardenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set to transparent theme
        setTheme(R.style.Theme_Transparent)

        if (intent?.action == Intent.ACTION_SEND) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                lifecycleScope.launch {
                    try {
                        saveToLinkwarden(sharedText)
                    } finally {
                        finish()
                    }
                }
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    private suspend fun saveToLinkwarden(url: String) {
        val encryptedSharedPrefs = try {
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyGenParameterSpec(
                    KeyGenParameterSpec.Builder(
                        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
                        .build()
                )
                .build()

            EncryptedSharedPreferences.create(
                applicationContext,
                "LinkwardenSettings",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ShareActivity, "Error accessing secure storage", Toast.LENGTH_LONG).show()
            }
            return
        }

        val server = encryptedSharedPrefs.getString("server", "") ?: ""
        val token = encryptedSharedPrefs.getString("token", "") ?: ""

        if (server.isEmpty() || token.isEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ShareActivity, "Please configure server and token first", Toast.LENGTH_LONG).show()
            }
            return
        }

        try {

            val retrofit = Retrofit.Builder()
                .baseUrl(server)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(LinkwardenApi::class.java)

            val request = LinkwardenRequest(
                url = url,
                title = url
            )

            val response = api.saveLink(
                auth = "Bearer $token",
                link = request
            )

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    println("Success response: ${response.body()}")
                    Toast.makeText(this@ShareActivity, "Link saved successfully!", Toast.LENGTH_LONG).show()
                } else {
                    println("Error response: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ShareActivity, "Failed to save link: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            println("Exception occurred: ${e.message}")
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ShareActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}


