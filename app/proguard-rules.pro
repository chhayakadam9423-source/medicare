# Proguard rules for Supabase models and Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.example.medicare.models.** { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
