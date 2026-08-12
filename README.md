# FIDS Android App

FIDS (Fitness & Inner Development System) is an Android mental health and personal development application built with Kotlin and Jetpack Compose. It provides psychological assessments, AI-powered counseling, breathing exercises, community features, and personal development roadmaps.

## Features

- **User Authentication** – Sign in / Sign up via Supabase Auth
- **Surveys & Psychological Tests** – Fill in assessments and receive instant results
- **AI Counseling Chat** – Chat with an AI counselor powered by Google Gemini (via REST API)
- **Result History** – View past survey and test results
- **Breathing Exercises** – Guided breathing to relieve stress
- **SOS Screen** – Quick access to emergency help and support
- **Development Roadmap** – 4 training projects (Improving the Classroom, Connecting Family, Phone Detox, Safety & Community)
- **FIDS Wiki** – Knowledge base about mental health topics
- **Skills Center** – Life skill details and guidance
- **Community** – Connect and share with the community
- **Profile** – Manage personal account information

## Tech Stack

| Layer      | Technology                                          |
|------------|-----------------------------------------------------|
| Language   | Kotlin                                              |
| UI         | Jetpack Compose, Material 3, Navigation Compose     |
| Backend    | Supabase (Auth, PostgREST, Storage)                 |
| AI Chat    | Google Gemini API via Ktor Client                   |
| Networking | Ktor Client, kotlinx.serialization                  |
| Storage    | DataStore Preferences                               |
| Image      | Coil                                                |

## Requirements

- Android Studio (latest stable version)
- JDK 17
- Android SDK 35 (compileSdk), minSdk 24

## Getting Started

1. Clone the repository:

   ```bash
   git clone https://github.com/HH1112HH/FIDS_ANDROID_APP.git
   ```

2. Open the project in Android Studio.
3. Copy `local.properties.example` to `local.properties` and fill in your keys:
   - **Supabase**: create a project on [Supabase](https://supabase.com) and paste the project URL and anon/publishable key.
   - **Gemini**: get an API key from [Google AI Studio](https://aistudio.google.com/).
   - The keys are injected at build time via `BuildConfig` and never stored in the repository.
4. Run the app on an emulator or physical device.

## Project Structure

```
app/src/main/java/com/example/fidsapp/
├── MainActivity.kt            # App entry point & navigation
├── LoginScreen.kt             # Login / Sign up
├── HomeScreen.kt              # Home dashboard
├── SurveyScreen.kt            # Surveys & psychological tests
├── ResultScreen.kt            # Survey / test results
├── HistoryScreen.kt           # Result history
├── ChatScreen.kt              # AI counseling chat UI
├── ChatViewModel.kt           # Gemini API integration
├── BreathingScreen.kt         # Breathing exercises
├── SOSScreen.kt               # Emergency help
├── RoadmapScreen.kt           # Development roadmap
├── ProjectDetailScreen.kt     # Project task details
├── WikiFidsScreen.kt          # Mental health knowledge base
├── SkillDetailScreen.kt       # Life skill details
├── CommunityScreen.kt         # Community features
├── ProfileScreen.kt           # User profile
├── SupabaseClient.kt          # Supabase configuration
└── ui/theme/                  # Compose theme
```

## Security Note

All secrets (Supabase URL/key, Gemini API key) are kept in `local.properties`, which is excluded from version control and injected at build time via `BuildConfig`. Never commit real keys — if a key was ever published, regenerate it in Supabase and Google AI Studio.

> **Important:** an older commit in this repository history may contain exposed keys. If you cloned this project and plan to use the same Supabase/Gemini accounts, regenerate those keys.

## License

This project is for educational purposes. No license is specified.
