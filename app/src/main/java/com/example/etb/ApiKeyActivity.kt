package com.example.etb

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.etb.databinding.ActivityApiKeyBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.io.FileWriter

class ApiKeyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApiKeyBinding
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApiKeyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Try to load saved API key
        val savedKey = loadApiKey()
        if (savedKey != null) {
            binding.etApiKey.setText(savedKey)
        }

        binding.btnTest.setOnClickListener {
            testConnection()
        }

        binding.btnStart.setOnClickListener {
            startPractice()
        }

        binding.btnHelp.setOnClickListener {
            binding.helpContent.visibility = if (binding.helpContent.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    private fun loadApiKey(): String? {
        return try {
            val configFile = File(filesDir, "config.json")
            if (configFile.exists()) {
                val config = gson.fromJson(configFile.readText(), JsonObject::class.java)
                if (config.has("api") && config.getAsJsonObject("api").has("openai_key")) {
                    config.getAsJsonObject("api").get("openai_key").asString
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun testConnection() {
        val key = binding.etApiKey.text.toString().trim()
        if (key.isEmpty()) {
            binding.tvStatus.text = "Please enter an API key first"
            binding.tvStatus.setTextColor(resources.getColor(R.color.red, theme))
            return
        }

        binding.tvStatus.text = "Testing connection..."
        binding.tvStatus.setTextColor(resources.getColor(R.color.blue, theme))

        Thread {
            try {
                val client = OkHttpClient()
                val mediaType = "application/json".toMediaTypeOrNull()
                val json = JSONObject().apply {
                    put("model", "gpt-4o")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Just say 'OK'")
                        })
                    })
                    put("temperature", 0.7)
                    put("max_tokens", 5)
                }
                val body = RequestBody.create(mediaType, json.toString())
                val request = Request.Builder()
                    .url("https://models.inference.ai.azure.com/chat/completions")
                    .header("Content-Type", "application/json")
                    .header("api-key", key)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    runOnUiThread {
                        binding.tvStatus.text = "Connection successful!"
                        binding.tvStatus.setTextColor(resources.getColor(R.color.green, theme))
                    }
                } else {
                    val errorBody = response.body?.string()
                    throw Exception("API request failed: ${response.code} - $errorBody")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.tvStatus.text = "Connection failed: ${e.message}"
                    binding.tvStatus.setTextColor(resources.getColor(R.color.red, theme))
                }
            }
        }.start()
    }

    private fun startPractice() {
        val key = binding.etApiKey.text.toString().trim()
        if (key.isEmpty()) {
            binding.tvStatus.text = "API key is required"
            binding.tvStatus.setTextColor(resources.getColor(R.color.red, theme))
            return
        }

        try {
            val config = JsonObject()
            val apiConfig = JsonObject()
            apiConfig.addProperty("openai_key", key)
            apiConfig.addProperty("base_url", "https://models.inference.ai.azure.com")
            config.add("api", apiConfig)

            val configFile = File(filesDir, "config.json")
            FileWriter(configFile).use { writer ->
                writer.write(gson.toJson(config))
            }

            val intent = Intent(this, PracticeActivity::class.java)
            intent.putExtra("api_key", key)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            binding.tvStatus.text = "Warning: Couldn't save settings (${e.message})"
            binding.tvStatus.setTextColor(resources.getColor(R.color.red, theme))
        }
    }
} 