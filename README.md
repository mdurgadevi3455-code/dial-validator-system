📱 SIM Activation App
An Android application that validates Indian mobile phone numbers, saves them to device contacts, and provides a full Contact Manager to view, edit, delete, and share contacts.

Features
🔢 Phone Validator

Validates that the number is exactly 10 digits and starts with 6, 7, 8, or 9 (valid Indian mobile prefixes)
Detects if the number already exists in contacts before offering to save
Saves new contacts directly to the device via Android's ContactsContract API

👥 Contact Manager

View all saved contacts in a list with avatar icons
Edit contact name and phone number
Delete contacts with a confirmation prompt
Share contact details via any installed sharing app (WhatsApp, SMS, Email, etc.)

🌐 Backend Integration

Retrofit client wired to a local backend for server-side phone validation
Endpoint: GET /phone/validate?phone={number}

🎨 UI

Clean dark-themed interface built with Material3
Custom drawable assets: circular avatar and phonebook icon


Tech Stack
LayerTechnologyLanguageJavaMin SDK24 (Android 7.0)Target SDK36UIXML Layouts + Material3HTTP ClientRetrofit 2.9.0 + Gson + ScalarsBuild SystemGradle (Kotlin DSL)

Project Structure
app/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    ├── ic_launcher-playstore.png
    ├── java/com/example/simactivationapp/
    │   ├── MainActivity.java              # Phone validation: validate & save contact
    │   ├── ContactsManagerActivity.java   # Contact Manager: list, edit, delete, share
    │   ├── ApiService.java                # Retrofit interface for /phone/validate
    │   ├── RetrofitClient.java            # Retrofit singleton (base: http://10.0.2.2:8080/)
    │   ├── model/
    │   │   └── PhoneRequest.java          # Request model for phone API
    │   └── utils/
    │       └── ContactHelper.java         # Utility: look up contact name by phone number
    └── res/
        ├── drawable/
        │   ├── circle_avatar.xml          # Circular avatar shape for contact list
        │   ├── ic_launcher_background.xml
        │   └── ic_phonebook.xml           # Phonebook icon used in UI
        └── layout/
            └── ...

Screens
ScreenDescriptionMainActivityEnter phone number → Validate → Save to contactsContactsManagerActivityBrowse all contacts → Edit / Delete / Share

Contact Manager — Feature Details
✏️ Edit Contact

Tap a contact to open an edit dialog
Update the name and/or phone number
Changes are saved back to device contacts via ContentResolver

🗑️ Delete Contact

Tap the delete option on any contact
A confirmation dialog prevents accidental deletion
Contact is permanently removed from device contacts

📤 Share Contact

Tap the share option on any contact
Opens the Android share sheet
Share name + number via WhatsApp, SMS, Email, or any installed app


Permissions
Declared in AndroidManifest.xml:
xml<uses-permission android:name="android.permission.READ_CONTACTS"/>
<uses-permission android:name="android.permission.WRITE_CONTACTS"/>
<uses-permission android:name="android.permission.INTERNET"/>
Runtime permissions for READ_CONTACTS and WRITE_CONTACTS are requested on app launch.

Validation Rules
A phone number is considered valid if:

It is exactly 10 digits long
The first digit is 6, 7, 8, or 9

If the number already exists in device contacts, it shows "Already in Contacts ✔" and hides the save button.

Backend / API (Optional)
The app includes a Retrofit setup for a Spring Boot (or similar) backend running locally:

Base URL: http://10.0.2.2:8080/ (Android emulator localhost)
Endpoint: GET /phone/validate?phone={number}
Response: Plain string


⚠️ android:usesCleartextTraffic="true" is enabled in the manifest to allow HTTP on the emulator. Use HTTPS in production.


Getting Started
Prerequisites

Android Studio (Ladybug or newer recommended)
JDK 21
Android Emulator (Pixel 4 AVD or equivalent) or a physical device running API 24+

Run the App

Clone the repository
Open in Android Studio
Sync Gradle
Run on an emulator or physical device

Run with Backend

Start your Spring Boot backend on localhost:8080
Launch the Android emulator — 10.0.2.2 maps to your machine's localhost
The Retrofit client will connect automatically
Dependencies
toml# gradle/libs.versions.toml
appcompat        = "1.6.1"
material         = "1.10.0"
activity         = "1.8.0"
constraintlayout = "2.1.4"

# build.gradle.kts (app)
retrofit2        = "2.9.0"   # + gson & scalars converters

License
This project is for educational/demo purposes.
