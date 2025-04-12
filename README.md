# English Practice Assistant

<div align="center">
  <img src="app/src/main/res/drawable/screenshot1.png" alt="Main Conversation Screen" width="300"/>
  <img src="app/src/main/res/drawable/screenshot2.png" alt="Settings Screen" width="300"/>
</div>

An Android application that helps users practice English conversation using AI-powered responses. The app provides a chat-like interface where users can practice their English skills with an AI assistant.

## 🚀 Features

- 🤖 **AI Conversation Practice**: Practice English with an AI assistant powered by Azure OpenAI
- 🎤 **Voice Input**: Speak your messages instead of typing
- 🔊 **Text-to-Speech**: Listen to AI responses with natural voice
- 📝 **Conversation History**: View and manage your past conversations
- 🔄 **Repeat Function**: Replay the last AI response
- 🆕 **New Topics**: Start fresh conversations anytime
- 🔑 **API Management**: Easy API key setup and management
- ⏱️ **Daily Limits**: Track your 50 daily responses

## 📋 Prerequisites

- Android Studio (Latest Version)
- Android Device/Emulator (API 24 or higher)
- Azure OpenAI Account
- Internet Connection

## ⚙️ Technical Specifications

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34
- **Kotlin Version**: 1.9.0
- **Gradle Version**: 8.11.1

### Dependencies

```gradle
implementation 'androidx.core:core-ktx:1.12.0'
implementation 'com.google.android.material:material:1.11.0'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
```

## 🛠️ Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/english-practice-assistant.git
   ```

2. Open the project in Android Studio

3. Build and run the application

4. Set up your Azure OpenAI API key:
   - Go to Settings
   - Enter your API key
   - Save and verify the connection

## 💡 Usage Guide

### Starting a Conversation

1. Launch the app
2. Type your message or use voice input
3. Tap the send button
4. Wait for the AI response

### Using Voice Input

1. Tap the microphone button
2. Speak your message clearly
3. The app will convert your speech to text
4. Review and send the message

### Managing Conversations

- Tap "New Topic" to start fresh
- Use the repeat button to hear the last response
- Enable auto-read for automatic speech
- View your conversation history

## 🔧 API Configuration

The app uses Azure OpenAI's GPT-4 model:

- Endpoint: `https://models.inference.ai.azure.com`
- Model: GPT-4
- Daily Limit: 50 responses

### API Key Setup

1. Get your Azure OpenAI API key
2. Enter it in the app settings
3. Test the connection
4. Start practicing!

## ⚠️ Troubleshooting

### Common Issues

- **API Errors**: Try generating a new API key
- **No Response**: Check internet connection
- **Voice Input Issues**: Ensure microphone permissions
- **Daily Limit Reached**: Wait for next day or contact support

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Azure OpenAI for providing the AI capabilities
- Android development community
- Material Design for UI components
- OkHttp for network requests

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

For support, email support@example.com or open an issue in the repository.
