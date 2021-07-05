# This is a configuration file for R8

-verbose
-allowaccessmodification
-repackageclasses

# Note that you cannot just include these flags in your own
# configuration file; if you are including this file, optimization
# will be turned off. You'll need to either edit this file, or
# duplicate the contents of this file and remove the include of this
# file from your project's proguard.config path property.

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# We only need to keep ComposeView + FragmentContainerView
-keep public class androidx.compose.ui.platform.ComposeView {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# For enumeration classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# AndroidX + support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn androidx.**

-keepattributes SourceFile,LineNumberTable
-keepattributes RuntimeVisibleAnnotations,
                RuntimeVisibleParameterAnnotations,
                RuntimeVisibleTypeAnnotations,
                AnnotationDefault
-renamesourcefileattribute SourceFile

-dontwarn org.conscrypt.**

# Dagger
-dontwarn com.google.errorprone.annotations.*

# Keep trakt-java and tmdb-java entity names (for GSON)
-keep class com.uwetrottmann.*.entities.** {
    <fields>;
    <init>(...);
}
-keep class com.uwetrottmann.*.enums.** {
    <fields>;
    <init>(...);
}

# Can remove this once we update to Navigation 2.4.0-alpha04
# See: https://issuetracker.google.com/191654433
-keep,allowobfuscation,allowshrinking class * extends androidx.navigation.Navigator

# Retain the generic signature of retrofit2.Call until added to Retrofit.
# Issue: https://github.com/square/retrofit/issues/3580.
# Pull request: https://github.com/square/retrofit/pull/3579.
-keep,allowobfuscation,allowshrinking class retrofit2.Call

# Retain annotation default values for all annotations.
# Required until R8 version >= 3.1.12-dev (expected in AGP 7.1.0-alpha4).
-keep,allowobfuscation,allowshrinking @interface *
