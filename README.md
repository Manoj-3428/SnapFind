# 📱 SnapFind - Lost & Found App

SnapFind is an Android application that helps users quickly report and find lost items using a community-driven approach. It uses Firebase Firestore for storing complaints and Firebase Realtime Database for managing user profiles, ensuring real-time updates and secure cloud integration.

---

## 🚀 Features

- 📍 **Post Lost Items:** Upload image, description, and reward for missing items.
- 🧭 **Live Location Tags:** Auto-fetch your location using GPS.
- 🔎 **Filter Complaints:** Filter by village, district, and state.
- 🧾 **User Profiles:** Users can update their personal info and location.
- 💬 **Chat System:** Message the finder directly using built-in messaging.
- 🗑️ **Auto-Delete Complaints:** All complaints are automatically deleted from Firestore after 7–10 days to keep the system clean.
- 📦 **Firebase Integration:**
  - Firestore for storing complaints.
  - Realtime Database for user profiles.
  - Firebase Storage for profile and complaint images.

---

## ⚙️ How It Works

1. **User Registration & Profile Setup:**
   - Users sign up using Firebase Authentication.
   - They can update their profile with name, photo, phone, and live location.
   - Profile data is stored in Firebase Realtime Database.

2. **Posting a Complaint:**
   - A user can upload an image of a lost item and provide a description, type, location, and optional reward.
   - The complaint is stored in Firebase Firestore along with timestamp and geolocation.
   - Complaints are scheduled to be auto-deleted after 7–10 days.

3. **Browsing & Filtering:**
   - All complaints are displayed in the Home screen.
   - Users can search by username or filter based on village, district, or state.

4. **Live Location:**
   - Location data is fetched using Google Location API.
   - When posting, the app automatically tags the location.

5. **Chat System:**
   - Users can directly message others regarding complaints.
   - Firebase stores the conversation data to enable real-time chat.

6. **Admin & Community Feedback:**
   - The app can be extended to let authorities mark complaints as resolved.
   - Resolved complaints appear with a green badge and updated status.

---

## 🔧 Tech Stack

| Component      | Technology              |
|----------------|--------------------------|
| UI             | Jetpack Compose + M3     |
| Language       | Kotlin                   |
| Architecture   | MVVM                     |
| Backend        | Firebase Firestore & RTDB|
| Auth           | Firebase Authentication  |
| Location       | Google Location API      |
| Image Storage  | Firebase Storage         |

---

## 🏁 Setup Instructions

1. **Clone the repo:**
   ```bash
   git clone https://github.com/Manoj-3428/SnapFind.git
   ```

2. **Open in Android Studio**

3. **Firebase Setup:**
   - Add `google-services.json` to `app/` folder.
   - Enable Firestore, Realtime Database, Authentication, and Storage in Firebase Console.

4. **Run the App** on emulator or physical device.

---

## 📁 Project Structure

```
├── model
│   ├── Chat.kt
│   ├── Complaint.kt
│   ├── LocationDetails.kt
│   ├── Message.kt
│   ├── Passing.kt
│   └── Profiles.kt
│
├── presentation
│   ├── animation/
│   ├── authentication/
│   ├── onboarding/
│   │   ├── AddComplaintScreen.kt
│   │   ├── CallScreen.kt
│   │   ├── ChatScreen.kt
│   │   ├── ComplaintScreen.kt
│   │   ├── DetailScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── MessageScreen.kt
│   │   └── ProfileScreen.kt
│
├── ui.theme/
│
├── viewmodel
│   ├── AuthViewModel.kt
│   ├── ComplaintViewModel.kt
│   ├── LocationProvider.kt
│   ├── newUser.kt
│   ├── oldUser.kt
│   └── firebase/
│       ├── ComplaintData.kt
│       ├── createChat.kt
│       ├── DeleteMyComplaint.kt
│       ├── DeleteMyImage.kt
│       ├── DeleteMyMessage.kt
│       ├── MyFirebaseMessagingService.kt
│       ├── NotificationHelper.kt
│       ├── profileLoader.kt
│       ├── profiles.kt
│       └── store.kt
│
├── MainActivity.kt
```

---

## 🧑‍💻 Developed By

**Manoj Kumar**  
Email: kommanamanojkumar830@gmail.com  
GitHub: [@Manoj-3428](https://github.com/Manoj-3428)

---
