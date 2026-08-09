-dontwarn org.apache.**
-dontwarn okhttp3.internal.**

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# 抛出异常时保留代码行号
-keepattributes SourceFile, LineNumberTable

-keepclasseswithmembernames class * {
    native <methods>;
}

-dontwarn androidx.compose.ui.**

