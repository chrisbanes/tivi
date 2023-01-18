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

# We only need to keep ComposeView
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

-keepattributes SourceFile,
                LineNumberTable,
                RuntimeVisibleAnnotations,
                RuntimeVisibleParameterAnnotations,
                RuntimeVisibleTypeAnnotations,
                AnnotationDefault

-renamesourcefileattribute SourceFile

-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Dagger
-dontwarn com.google.errorprone.annotations.*

# Retain the generic signature of retrofit2.Call until added to Retrofit.
# Issue: https://github.com/square/retrofit/issues/3580.
# Pull request: https://github.com/square/retrofit/pull/3579.
-keep,allowobfuscation,allowshrinking class retrofit2.Call

# See https://issuetracker.google.com/issues/265188224
-keep,allowshrinking class * extends androidx.compose.ui.node.ModifierNodeElement {}
