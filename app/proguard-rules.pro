# glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Android Support Library
-dontwarn android.support.**
-keep class android.support.** { *; }

# Gradle Retrolambda Plugin
-dontwarn java.lang.invoke.*

-keep public class * extends java.lang.Exception
-printmapping mapping.txt

# okio
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# jsoup
-keep public class org.jsoup.** { public *; }

# dagger2
-dontwarn com.google.errorprone.annotations.*

# gson
-keepnames class hm.orz.chaos114.android.slideviewer.infra.model.** { *; }

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

# Dynamic class load by reflection
-keep public class * extends hm.orz.chaos114.android.slideviewer.ocr.OcrRecognizer
