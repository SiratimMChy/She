<div align="center">
  <h1>🚨 She - Women's Safety Application</h1>
  <p><strong>An Android application designed to provide reliable emergency assistance and personal safety features.</strong></p>

  <!-- Badges -->
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Language-Java-007396?style=flat&logo=java&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=flat&logo=firebase&logoColor=black" alt="Firebase" />
</div>

<br/>

## 📖 Overview

**She** is a safety-focused Android application developed as a 3rd-year academic project. Its primary goal is to provide a quick and reliable way for women to seek help during emergencies. By pressing a single SOS button—or using hardware triggers like shaking the device—the app immediately alerts trusted contacts and shares the user's live location, ensuring timely assistance. 

## ✨ Features

- **🚨 Hardware-Triggered SOS Alerts:** 
  - **Shake to SOS:** Built-in accelerometer detection allows users to send an SOS simply by shaking the phone 3 times.
  - **Volume Button SOS:** Users can press the Volume Down button 3 times consecutively to silently trigger the emergency alert.
- **📍 Location-Aware Messaging:** Emergency SMS alerts automatically include a Google Maps link pinpointing the user's real-time GPS coordinates.
- **💬 Custom SMS Integration:** A dedicated messaging fragment allows users to compose and send custom text messages directly to specific trusted contacts.
- **👥 Trusted Contacts Management:** Securely add, manage, and retrieve primary emergency contacts via Firebase Realtime Database.
- **🗺️ Police Station Locator:** Retrieves and displays a list of verified nearby police stations directly from the Firebase Realtime Database.
- **🛡️ Safety Guidelines & Blogs:** A dedicated section containing curated personal safety tips and awareness blogs.
- **🔐 Secure User Accounts:** Powered by Firebase Authentication to keep user data private and manage sessions safely.
- **⚙️ Admin Dashboard:** A control panel for administrators to manage verified police station data and oversee app resources.

## 🛠️ Technology Stack

- **Platform:** Android
- **Language:** Java
- **UI Components:** Material Design, XML, ViewBinding, RecyclerView, BottomNavigationView, DrawerLayout
- **Image Loading:** Glide
- **Backend & Database:** Firebase Authentication, Firebase Realtime Database
- **Hardware & Sensors:** Android SensorManager (Accelerometer), KeyEvent Handling
- **Maps & Location:** Google Maps SDK, FusedLocationProviderClient
- **Networking:** Native Android networking and Firebase SDK

## 🚀 Installation Guide

To run this project locally, follow the steps below.

### Prerequisites
- Android Studio
- Java Development Kit (JDK 17+)
- A **Google Maps API Key** (obtainable from the Google Cloud Console)

### Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/SiratimMChy/She.git
   ```

2. **Open the project:** 
   Launch Android Studio, select `File` > `Open`, and choose the cloned directory.

3. **Configure Google Maps API:** 
   Open the `local.properties` file in the root directory of the project and add your API key:
   ```properties
   MAPS_API_KEY=your_actual_api_key
   ```

4. **Connect to Firebase:** 
   The project includes a `google-services.json` file. If you wish to connect it to your own Firebase instance, go to `Tools` > `Firebase` in Android Studio and follow the setup wizard.

5. **Build and Run:** 
   Sync the Gradle files and run the application on an emulator or physical device.

## 📱 Screenshots

*(Application screenshots will be added here)*

| Home Screen | SOS Alert | Maps Integration |
| :---: | :---: | :---: |
| <img src="https://via.placeholder.com/200x400.png?text=Home+Screen" width="200"/> | <img src="https://via.placeholder.com/200x400.png?text=SOS+Alert" width="200"/> | <img src="https://via.placeholder.com/200x400.png?text=Map+View" width="200"/> |

## 🌱 Future Development

Planned features for upcoming updates include:
- Voice-activated SOS commands.
- Offline SMS alerts when an internet connection is unavailable.
- Integration of a hospital locator for medical emergencies.

## 👨‍💻 Developers

Developed by:
- **Siratim Mustakim Chowdhury**
- **Ruhit Dhar Raz**

---
