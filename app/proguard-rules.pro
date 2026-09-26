# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep domain models for Firestore serialization
-keep class com.mymusic.app.domain.model.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep Media3
-keep class androidx.media3.** { *; }
