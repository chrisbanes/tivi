# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-dontpreverify

# Optimize all the things (other than those listed)
-optimizations !field/*

-allowaccessmodification
-repackageclasses ''

# Note that you cannot just include these flags in your own
# configuration file; if you are including this file, optimization
# will be turned off. You'll need to either edit this file, or
# duplicate the contents of this file and remove the include of this
# file from your project's proguard.config path property.

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Fragment
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# AndroidX + support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn androidx.**

-keep class com.google.android.material.theme.MaterialComponentsViewInflater

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-renamesourcefileattribute SourceFile

# Fabric/Crashlytics
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# Dagger
-dontwarn com.google.errorprone.annotations.*

# Retrofit
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature, InnerClasses, Exceptions
# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Okhttp + Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Keep Trakt-java Entity names (for GSON)
-keepclassmembernames class com.uwetrottmann.trakt5.entities.** { <fields>; }
-keepclassmembers class com.uwetrottmann.trakt5.entities.** { <init>(...); }

# Keep TMDb Entity names (for GSON)
-keepclassmembernames class com.uwetrottmann.tmdb2.entities.** { <fields>; }
-keepclassmembers class com.uwetrottmann.tmdb2.entities.** { <init>(...); }

# !! Remove this once https://issuetracker.google.com/issues/112386012 is fixed !!
-keep class com.uwetrottmann.trakt5.entities.**
-keep class com.uwetrottmann.tmdb2.entities.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
# Keep Coroutine class names. See https://github.com/Kotlin/kotlinx.coroutines/issues/657.
# This should be removed when bug is fixed.
-keepnames class kotlinx.** { *; }

-dontwarn org.jetbrains.annotations.**
-keep class kotlin.Metadata { *; }

# Kotlin Reflect internal impl
-keep public class kotlin.reflect.jvm.internal.impl.builtins.* { public *; }
-keep public class kotlin.reflect.jvm.internal.impl.serialization.deserialization.builtins.* { public *; }

# Need to keep class name due to kotlin-reflect
-keep interface com.airbnb.mvrx.MvRxState
# !! Tweak this once https://issuetracker.google.com/issues/112386012 is fixed !!
# Need to keep class name due to kotlin-reflect
-keep class * implements com.airbnb.mvrx.MvRxState { *; }

# Need to keep MvRxViewModelFactory companion classes for MvRx
# See https://github.com/airbnb/MvRx/issues/185
-keepnames class * implements com.airbnb.mvrx.MvRxViewModelFactory

# Keep FAB show/hide methods for MotionLayout KeyTriggers
-keep class * extends com.google.android.material.floatingactionbutton.FloatingActionButton {
    public void show();
    public void hide();
}