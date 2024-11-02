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
import java.net.URL

class ShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Transparent)

        if (intent?.action == Intent.ACTION_SEND) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

            if (sharedText != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        saveToLinkwarden(sharedText, sharedSubject)
                    } catch (e: Exception) {
                        showToast("Unexpected error occurred")
                    } finally {
                        withContext(Dispatchers.Main) {
                            finish()
                        }
                    }
                }
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    private suspend fun saveToLinkwarden(url: String, subject: String?) {
        val encryptedSharedPrefs = getEncryptedSharedPreferences()

        val server = encryptedSharedPrefs.getString("server", "") ?: ""
        val token = encryptedSharedPrefs.getString("token", "") ?: ""
        val tags = encryptedSharedPrefs.getString("tags", "") ?: ""
        val collectionId = encryptedSharedPrefs.getString("collectionId", null)

        if (server.isEmpty() || token.isEmpty()) {
            showToast("Please configure server and token first")
            return
        }

        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(server)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(LinkwardenApi::class.java)

            val name = subject ?: extractTitleFromUrl(url)

            val request = LinkwardenRequest(
                url = url,
                name = name,
                description = "",
                type = "url",
                tags = tags.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { LinkwardenRequest.Tag(it) },
                collectionId = collectionId?.toIntOrNull()?.let { LinkwardenRequest.Collection(it) }
            )

            val response = api.saveLink(
                auth = "Bearer $token",
                link = request
            )

            if (response.isSuccessful) {
                showToast("Link saved successfully!")
            } else {
                showToast("Failed to save link: ${response.code()}")
            }
        } catch (e: Exception) {
            showToast("Error: ${e.message}")
        }
    }

    private fun extractTitleFromUrl(url: String): String {
        return try {
            URL(url).host
        } catch (e: Exception) {
            url
        }
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@ShareActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getEncryptedSharedPreferences(): EncryptedSharedPreferences {
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

        return EncryptedSharedPreferences.create(
            applicationContext,
            "LinkwardenSettings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }
}
