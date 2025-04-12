# English Practice Assistant

An Android application that helps users practice English conversation using AI-powered responses. The app provides a chat-like interface where users can practice their English skills with an AI assistant.

## Screenshots

<div align="center">
  <img src="app/src/main/res/drawable/screenshot1.png" alt="Main Conversation Screen" width="300"/>
  <img src="app/src/main/res/drawable/screenshot2.png" alt="Settings Screen" width="300"/>
</div>

> Note: To add screenshots:
>
> 1. Take screenshots of your app
> 2. Save them as `screenshot1.png` and `screenshot2.png`
> 3. Place them in the `app/src/main/res/drawable/` directory

## Features

- 🤖 AI-powered English conversation practice
- 🎤 Voice input support
- 🔊 Text-to-speech for AI responses
- 📝 Message history management
- 🔄 Repeat last message functionality
- 🆕 Start new conversation topics
- 🔑 API key management
- ⏱️ Daily response limit tracking (50 responses per day)

## Technical Requirements

- Android Studio
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34
- Kotlin version: 1.9.0
- Gradle version: 8.11.1

## Dependencies

- AndroidX Core
- Material Design Components
- OkHttp for API calls
- Android Speech Recognition
- Text-to-Speech Engine

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Build and run the application
4. Set up your Azure OpenAI API key in the settings

## API Configuration

The app uses Azure OpenAI's GPT-4 model for generating responses. You'll need to:

1. Have an Azure OpenAI account
2. Set up your API key in the app settings
3. Use the correct endpoint URL: `https://models.inference.ai.azure.com`

## Usage Guide

1. **Starting a Conversation**

   - Launch the app
   - Type or speak your message
   - Tap the send button or use voice input

2. **Voice Input**

   - Tap the microphone button
   - Speak your message
   - The app will convert speech to text

3. **Text-to-Speech**

   - Enable auto-read in settings
   - AI responses will be read aloud automatically

4. **Managing Conversations**
   - Use "New Topic" to start fresh conversations
   - Tap the repeat button to hear the last response again
   - View your conversation history

## Error Handling

- If you encounter API errors, try generating a new API key
- Check your internet connection if responses aren't loading
- Ensure you have remaining daily responses available

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Azure OpenAI for providing the AI capabilities
- Android development community for resources and support
