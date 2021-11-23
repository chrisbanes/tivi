# Chucker uses GSON and TypeToken:
# https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#gson-with-full-mode
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken