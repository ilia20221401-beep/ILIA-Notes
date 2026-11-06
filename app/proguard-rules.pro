# ILIA Notes Proguard & R8 Optimization Rules

# Keep Room Database and DAO classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.example.data.model.** { *; }
-keep interface com.example.data.local.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <methods>;
}

# Preserve Room annotations
-keepattributes *Annotation*
-dontwarn androidx.room.paging.**

# Keep Kotlin reflection & Coroutines
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
