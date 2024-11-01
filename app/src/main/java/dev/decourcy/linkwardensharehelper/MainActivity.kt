package dev.decourcy.linkwardensharehelper

import android.content.Context
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class MainActivity : AppCompatActivity() {
    private val sharedPrefsFile = "LinkwardenSettings"
    private lateinit var encryptedSharedPrefs: EncryptedSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupEncryptedSharedPreferences()

        // Load saved settings
        findViewById<EditText>(R.id.serverAddress).setText(
            encryptedSharedPrefs.getString("server", "")
        )
        findViewById<EditText>(R.id.accessToken).setText(
            encryptedSharedPrefs.getString("token", "")
        )

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveSettings()
        }
    }

    private fun setupEncryptedSharedPreferences() {
        try {
            val masterKeySpec = KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
                .build()

            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyGenParameterSpec(masterKeySpec)
                .build()

            encryptedSharedPrefs = EncryptedSharedPreferences.create(
                applicationContext,
                sharedPrefsFile,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Error initializing secure storage",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun saveSettings() {
        val server = findViewById<EditText>(R.id.serverAddress).text.toString()
        val token = findViewById<EditText>(R.id.accessToken).text.toString()

        try {
            encryptedSharedPrefs.edit().apply {
                putString("server", server)
                putString("token", token)
                apply()
            }
            Toast.makeText(this, "Settings saved securely", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Error saving settings securely",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
