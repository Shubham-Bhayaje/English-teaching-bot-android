package com.example.etb

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.etb.databinding.ActivityPracticeBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileWriter
import java.util.Locale
import java.util.UUID
import kotlin.concurrent.thread
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import android.app.AlertDialog
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.view.View

class PracticeActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityPracticeBinding
    private lateinit var adapter: MessageAdapter
    private var tts: TextToSpeech? = null
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isRecording = false
    private var apiKey: String? = null
    private var autoRead = true
    private val conversationHistory = mutableListOf<Message>()
    private var currentTopic: String? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPracticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiKey = intent.getStringExtra("api_key")

        setupRecyclerView()
        setupTextToSpeech()
        setupSpeechRecognizer()
        setupButtons()
        loadConversationHistory()

        // Show welcome message
        addMessage(Message("""Hello! I'm your English practice assistant. How can I help you today?

Note: You have 50 responses available per day. Use them wisely to practice your English conversation skills!

If you encounter any API errors, please try generating a new API key.""", false))
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(conversationHistory)
        binding.rvConversation.apply {
            layoutManager = LinearLayoutManager(this@PracticeActivity)
            adapter = this@PracticeActivity.adapter
        }
    }

    private fun setupTextToSpeech() {
        tts = TextToSpeech(this, this)
    }

    private fun setupSpeechRecognizer() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // Show explanation if needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                AlertDialog.Builder(this)
                    .setTitle("Microphone Permission Needed")
                    .setMessage("This app needs access to your microphone to recognize your speech. Please grant the permission to use voice input.")
                    .setPositiveButton("OK") { _, _ ->
                        requestMicrophonePermission()
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        Toast.makeText(this, "Voice input will not be available", Toast.LENGTH_SHORT).show()
                    }
                    .create()
                    .show()
            } else {
                requestMicrophonePermission()
            }
        } else {
            initializeSpeechRecognizer()
        }
    }

    private fun requestMicrophonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO_PERMISSION
        )
    }

    private fun initializeSpeechRecognizer() {
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isRecording = true
                    updateMicButton()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0]
                        binding.etMessage.setText(spokenText)
                        processInput(spokenText)
                    }
                    isRecording = false
                    updateMicButton()
                }

                override fun onError(error: Int) {
                    isRecording = false
                    updateMicButton()
                    val errorMessage = getErrorText(error)
                    runOnUiThread {
                        Toast.makeText(this@PracticeActivity, "Speech recognition error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to initialize speech recognizer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {
        binding.btnNewTopic.setOnClickListener {
            val topics = arrayOf(
                "Technology",
                "Travel",
                "Food",
                "Sports",
                "Movies",
                "Music",
                "Books",
                "Hobbies",
                "Current events",
                "Environmental issues"
            )
            val topic = topics.random()
            currentTopic = topic
            addMessage(Message("Let's talk about: $topic", false))
        }

        binding.switchAutoRead.setOnCheckedChangeListener { _, isChecked ->
            autoRead = isChecked
        }

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text?.toString() ?: ""
            if (message.isNotBlank()) {
                processInput(message)
                binding.etMessage.text?.clear()
            }
        }

        binding.btnRepeat.setOnClickListener {
            if (conversationHistory.isNotEmpty()) {
                val lastMessage = conversationHistory.last()
                if (!lastMessage.isUser) {
                    speak(lastMessage.text)
                }
            }
        }

        binding.fabVoice.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }

    private fun startRecording() {
        if (!::speechRecognizer.isInitialized) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            isRecording = true
            updateMicButton()
            binding.tvStatus.text = "Recording..."

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            isRecording = false
            updateMicButton()
            Toast.makeText(this, "Error starting speech recognition: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        if (!::speechRecognizer.isInitialized) {
            return
        }

        try {
            isRecording = false
            updateMicButton()
            speechRecognizer.stopListening()
        } catch (e: Exception) {
            Toast.makeText(this, "Error stopping speech recognition: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMicButton() {
        binding.fabVoice.setImageResource(
            if (isRecording) R.drawable.ic_mic_off else R.drawable.ic_mic
        )
        binding.fabVoice.setBackgroundColor(
            ContextCompat.getColor(this, if (isRecording) R.color.red else R.color.blue)
        )
    }

    private fun processInput(input: String) {
        if (input.isEmpty()) return

        if (input.startsWith("/")) {
            processCommand(input)
            return
        }

        addMessage(Message(input, true))
        binding.tvStatus.text = "Processing..."

        thread {
            try {
                val response = generateResponse(input)
                runOnUiThread {
                    addMessage(Message(response, false))
                    binding.tvStatus.text = "Ready"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    addMessage(Message("Sorry, I encountered an error: ${e.message}", false))
                    binding.tvStatus.text = "Error"
                }
            }
        }
    }

    private fun processCommand(command: String) {
        val parts = command.lowercase().split(" ")
        when (parts[0]) {
            "/topic" -> {
                val topics = listOf(
                    "Travel and vacation experiences",
                    "Hobbies and interests",
                    "Food and cooking",
                    "Movies and entertainment",
                    "Technology and gadgets",
                    "Work and career goals",
                    "Education and learning",
                    "Family and relationships",
                    "Cultural differences",
                    "Current events",
                    "Health and fitness",
                    "Environmental issues"
                )
                val topic = topics.random()
                addMessage(Message("Let's talk about: $topic", false))
            }
            "/difficulty" -> {
                if (parts.size > 1) {
                    val level = parts[1]
                    if (level in listOf("beginner", "intermediate", "advanced")) {
                        addMessage(Message("Difficulty set to $level", false))
                    } else {
                        addMessage(Message("Invalid difficulty level. Choose beginner, intermediate, or advanced.", false))
                    }
                }
            }
            "/test" -> {
                addMessage(Message("This is a test of the text-to-speech functionality.", false))
            }
            "/help", "/commands" -> {
                val helpText = """Available commands:
/topic - Suggest a new conversation topic
/difficulty [beginner|intermediate|advanced] - Set difficulty level
/test - Test the text-to-speech functionality
/help - Show this help message"""
                addMessage(Message(helpText, false))
            }
        }
    }

    private fun generateResponse(input: String): String {
        if (apiKey.isNullOrEmpty()) {
            throw Exception("API key is not set")
        }

        val client = OkHttpClient()
        val messages = mutableListOf(
            JsonObject().apply {
                addProperty("role", "system")
                addProperty("content", """You are an English conversation practice assistant helping users improve their English skills.
Current difficulty level: Beginner
Focus areas: conversation, grammar, vocabulary

Guidelines:
- Keep responses natural, friendly and conversational
- Use language appropriate for the Beginner level
- Ask follow-up questions to encourage conversation
- Provide gentle corrections for major mistakes without interrupting the flow
- Occasionally introduce new vocabulary or expressions with brief explanations
- Be encouraging and supportive
- Don't return emoji or special characters in your response.

If the user's message has English errors and correction mode is enabled, include a brief correction at the end of your response.

Current topic: ${currentTopic ?: "Open conversation"}""")
            }
        )

        conversationHistory.takeLast(5).forEach { message ->
            messages.add(JsonObject().apply {
                addProperty("role", if (message.isUser) "user" else "assistant")
                addProperty("content", message.text)
            })
        }

        messages.add(JsonObject().apply {
            addProperty("role", "user")
            addProperty("content", input)
        })

        val requestBody = JsonObject().apply {
            addProperty("model", "gpt-4o")
            add("messages", gson.toJsonTree(messages))
            addProperty("temperature", 0.7)
            addProperty("max_tokens", 200)
        }.toString()

        val request = Request.Builder()
            .url("https://models.inference.ai.azure.com/chat/completions")
            .header("api-key", apiKey ?: "")
            .header("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("API request failed: ${response.code}")
        }

        val responseBody = response.body?.string()
        val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
        return jsonResponse.getAsJsonArray("choices")
            .get(0)
            .asJsonObject
            .getAsJsonObject("message")
            .get("content")
            .asString
    }

    private fun addMessage(message: Message) {
        conversationHistory.add(message)
        adapter.notifyItemInserted(conversationHistory.size - 1)
        binding.rvConversation.smoothScrollToPosition(conversationHistory.size - 1)

        if (!message.isUser && autoRead) {
            speak(message.text)
        }

        saveConversationHistory()
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }

    private fun saveConversationHistory() {
        try {
            val historyFile = File(filesDir, "english_practice_history.json")
            FileWriter(historyFile).use { it.write(gson.toJson(conversationHistory)) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadConversationHistory() {
        try {
            val historyFile = File(filesDir, "english_practice_history.json")
            if (historyFile.exists()) {
                val history = gson.fromJson(historyFile.readText(), Array<Message>::class.java)
                conversationHistory.addAll(history)
                adapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(1.0f)
        } else {
            Toast.makeText(this, "Text-to-speech initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
        speechRecognizer.destroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeSpeechRecognizer()
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Microphone permission denied. Voice input will not be available.", Toast.LENGTH_LONG).show()
                binding.fabVoice.isEnabled = false
                binding.fabVoice.alpha = 0.5f
            }
        }
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}

data class Message(
    val text: String,
    val isUser: Boolean
) 