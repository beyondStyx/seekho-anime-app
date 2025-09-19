# **Anime Explorer**

An Android assignment project built in **Kotlin** using modern Android development practices.

The app fetches anime data from the [Jikan API](https://jikan.moe/) and provides offline caching, detail pages, and cast information.

## **Features**

* **Top Anime List** with pagination & pull-to-refresh (scroll refreshing \+ swipe-to-refresh)  
* **Detail Screen** with trailer (WebView) or poster fallback  
* **Trailer Watchdog** – if the trailer doesn’t load in **4 seconds**, the app gracefully falls back to showing the poster  
* **Cast List** (main characters) with images  
* **Offline Caching** via Room (list, detail, cast)  
* **Network-aware Sync** (auto-refreshes on reconnect)  
* **Splash Screen** with custom logo & background  
* **No-Images Mode** – toggle from toolbar to hide images and show placeholders/initials

## **Tech Stack**

* **Language**: Kotlin  
* **Architecture**: MVVM \+ ViewModel \+ StateFlow  
* **UI**: Material 3 (MaterialToolbar, Snackbar, SwipeRefresh, RecyclerView)  
* **Networking**: Retrofit \+ Moshi \+ OkHttp (with logging)  
* **Persistence**: Room (Anime, Details, Cast)  
* **Image Loading**: Glide  
* **Async**: Kotlin Coroutines & Flows  
* **Splash Screen**: AndroidX SplashScreen API  
* 

## **Setup**

1. Clone this repo  
2. Open in **Android Studio (Giraffe/Latest)**  
3. Build & run on an emulator or device with Android 7.0+

## **Known Limitations**

* **Unit Tests**: DAO & Repository tests were planned but skipped due to time constraints.  
* **ProGuard / R8**: Minimal rules for Moshi/Room/Glide not added (release builds may strip adapters).

## **Notes**

* Autoplay of trailers was disabled intentionally → better UX, avoids jarring experience, and respects WebView/browser autoplay policies.  
* If a trailer doesn’t load within **4 seconds**, the app automatically falls back to the poster.  
* Image toggle (Hide Images) allows lightweight browsing on slow networks.  
* Pull-to-refresh & scroll-based pagination are both supported for smooth browsing.

## **Author**

Assignment completed by **Lalit Kumar Meena** for **Seekho Android Developer Assignment**.

