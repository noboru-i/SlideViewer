# gson
-keepnames class hm.orz.chaos114.android.slideviewer.model.** { *; }

# glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# JavascriptInterface
-keepattributes JavascriptInterface
-keepclasseswithmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# leakcanary
-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }

# Android Support Library
-dontwarn android.support.**
-keep class android.support.** { *; }

# OrmLite
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-dontwarn com.j256.ormlite.android.**
-dontwarn com.j256.ormlite.logger.**
-dontwarn com.j256.ormlite.misc.**

# Gradle Retrolambda Plugin
-dontwarn java.lang.invoke.*

-keep public class * extends java.lang.Exception
-printmapping mapping.txt

# okio
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# jsoup
-keep public class org.jsoup.** { public *; }
