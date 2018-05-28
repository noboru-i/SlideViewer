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

# Dynamic class load by reflection
-keep public class hm.orz.chaos114.android.slideviewer.ocr.OcrRecognizer { *; }
