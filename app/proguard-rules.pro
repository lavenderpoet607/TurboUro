-keep class com.turbouro.app.model.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-dontwarn rikka.shizuku.**
-dontwarn moe.shizuku.**
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }
