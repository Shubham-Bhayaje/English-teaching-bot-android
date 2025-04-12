package com.example.etb

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        messageAdapter = MessageAdapter(messages)
        messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString()
            if (messageText.isNotEmpty()) {
                // Add user message
                messages.add(Message(messageText, true))
                messageAdapter.notifyItemInserted(messages.size - 1)
                messagesRecyclerView.scrollToPosition(messages.size - 1)
                messageInput.text.clear()

                // Simulate bot response (replace with actual bot logic)
                messages.add(Message("This is a simulated bot response", false))
                messageAdapter.notifyItemInserted(messages.size - 1)
                messagesRecyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }
} 