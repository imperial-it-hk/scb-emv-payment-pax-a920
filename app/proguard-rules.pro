# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\ADT\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# 代码混淆压缩比，在0~7之间，默认为5,一般不下需要修改
-optimizationpasses 5

# 混淆时不使用大小写混合，混淆后的类名为小写
# windows下的同学还是加入这个选项吧(windows大小写不敏感)
-dontusemixedcaseclassnames

# 指定不去忽略非公共的库的类
# 默认跳过，有些情况下编写的代码与类库中的类在同一个包下，并且持有包中内容的引用，此时就需要加入此条声明
# 对于 Proguard 4.5版本这个设置是默认的
-dontskipnonpubliclibraryclasses

# 指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers

# 不做预检验，preverify是proguard的四个步骤之一
# Android不需要preverify，去掉这一步可以加快混淆速度
-dontpreverify

# 有了verbose这句话，混淆后就会生成映射文件
# 包含有类名->混淆后类名的映射关系
# 然后使用printmapping指定映射文件的名称
-verbose
-printmapping priguardMapping.txt

# 指定混淆时采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不改变
-optimizations !code/simplification/artithmetic,!field/*,!class/merging/*

# 保护代码中的Annotation不被混淆
# 这在JSON实体映射时非常重要，比如fastJson
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }

# 避免混淆泛型
# 这在JSON实体映射时非常重要，比如fastJson
-keepattributes Signature

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 保留所有的本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留了继承自Activity、Application这些类的子类
# 因为这些子类有可能被外部调用
# 比如第一行就保证了所有Activity的子类不要被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService

# 保留Activity中的方法参数是view的方法，
# 从而我们在layout里面编写onClick就不会影响
-keepclassmembers class * extends android.app.Activity {
    public void * (android.view.View);
}

# 枚举类不能被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留自定义控件(继承自View)不能被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    *** get* ();
}

# 保留自定义控件(继承自View)不能被混淆
-keep public class * extends android.view.animation.Animation {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    *** get* ();
}

# 保留Parcelable序列化的类不能被混淆
-keep class * implements android.os.Parcelable{
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable 序列化的类不被混淆
-keep public class * implements java.io.Serializable {*;}
-keepclassmembers class * implements java.io.Serializable {
   static final long serialVersionUID;
   private static final java.io.ObjectStreamField[] serialPersistentFields;
   !static !transient <fields>;
   private void writeObject(java.io.ObjectOutputStream);
   private void readObject(java.io.ObjectInputStream);
   java.lang.Object writeReplace();
   java.lang.Object readResolve();
}

# 对R文件下的所有类及其方法，都不能被混淆
-keepclassmembers class **.R$* {
    *;
}

# 对于带有回调函数onXXEvent的，不能混淆
-keepclassmembers class * {
    void *(**On*Event);
}

#support
-dontwarn android.support.**
-keep class android.support.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep interface android.support.v7.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.app.Fragment

#OrmLite
#-libraryjars libs/ormlite-android-5.0.jar
#-libraryjars libs/ormlite-core-5.0.jar
-dontwarn com.j256.ormlite.**
-keep class com.j256.ormlite.** { *;}
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-keepclassmembers class * {@com.j256.ormlite.field.DatabaseField *;}

-keep public class javax.**

-dontwarn org.apache.commons.mail.**
-keep class org.apache.commons.mail.**

-dontwarn org.apache.harmony.awt.**
-keep class org.apache.harmony.awt.**

-dontwarn com.sun.mail.smtp.**
-keep class com.sun.mail.smtp.**

-dontwarn com.sun.mail.imap.**
-keep class com.sun.mail.imap.**
-keep class com.sun.mail.handlers.**
-keepclassmembers class com.sun.mail.** { *; }
# for no object dch for mime type multipart/mixed
-keepclassmembers class com.sun.mail.handlers.** { *; }


-dontwarn javax.activation.**
-keep class javax.activation.**

-dontwarn com.pax.api.**
-keep class com.pax.api.** { *; }
-keep class net.sqlcipher.** { *; }
-keep class com.pax.ipp.** { *; }
-dontwarn com.pax.gl.**
-keep class com.pax.gl.** { *; }
-dontwarn com.pax.jemv.**
-keep class com.pax.jemv.** { *; }
-keep class com.pax.utils.** { *; }
-dontwarn com.pax.neptunelite.api.**
-keep class com.pax.neptunelite.api.**{ *; }
-keep class com.pax.dal.**{ *; }
-keep class com.pax.eemv.**{ *; }

#net.sqlcipher
-dontwarn net.sqlcipher.**
-keep public class net.sqlcipher.** { *;}
-keep public class net.sqlcipher.database.** { *;}

-keep class cn.bingoogolapple.bgabanner.** { *;}

-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(Java.lang.Throwable);
}

-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }

#不混淆AppStore
-dontwarn com.pax.market.api.sdk.java.**
-keep class com.pax.appstore.dto.** { *; }

### greenDAO 3
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties { *; }
# If you DO use SQLCipher:
-keep class org.greenrobot.greendao.database.SqlCipherEncryptedHelper { *; }

# If you do not use SQLCipher:
# -dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
 -dontwarn rx.**

 # 如果按照上面介绍的加入了数据库加密功能，则需添加一下配置
 #sqlcipher数据库加密开始
 -keep  class net.sqlcipher.** {*;}
 -keep  class net.sqlcipher.database.** {*;}
 #sqlcipher数据库加密结束


 #Gson
 -dontwarn com.google.gson.**
 -keep class sun.misc.Unsafe { *; }
 -keep class com.google.gson.** { *; }
 -keep class com.google.gson.examples.android.model.** { *; }

 #JJWT
 -keepnames class com.fasterxml.jackson.databind.** { *; }
 -dontwarn com.fasterxml.jackson.databind.*
 -keepattributes InnerClasses
 -keep class org.bouncycastle.** { *; }
 -keepnames class org.bouncycastle.** { *; }
 -dontwarn org.bouncycastle.**
 -keep class io.jsonwebtoken.** { *; }
 -keepnames class io.jsonwebtoken.* { *; }
 -keepnames interface io.jsonwebtoken.* { *; }
 -dontwarn javax.xml.bind.DatatypeConverter
 -dontwarn io.jsonwebtoken.impl.Base64Codec
 -keepnames class com.fasterxml.jackson.** { *; }
 -keepnames interface com.fasterxml.jackson.** { *; }

 #dom4j
 -dontwarn org.dom4j.**
 -keep class org.dom4j.**{*;}
 -dontwarn org.xml.sax.**
 -keep class org.xml.sax.**{*;}
 -dontwarn com.fasterxml.jackson.**
 -keep class com.fasterxml.jackson.**{*;}
 -dontwarn com.pax.market.api.sdk.java.base.util.**
 -keep class com.pax.market.api.sdk.java.base.util.**{*;}
 -dontwarn org.w3c.dom.**
 -keep class org.w3c.dom.**{*;}
 -dontwarn javax.xml.**
 -keep class javax.xml.**{*;}

 #dto
 -dontwarn com.pax.market.api.sdk.java.base.dto.**
 -keep class com.pax.market.api.sdk.java.base.dto.**{*;}

 -dontwarn com.pax.unifiedsdk.**
 -keep class com.pax.unifiedsdk.** {*;}


# Glide start
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Glide end

# RxJava RxAndroid start
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
# RxJava RxAndroid end

-dontwarn com.google.errorprone.annotations.*
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit


-keep class ch.qos.** { *; }
-keep class org.slf4j.** { *; }
-keepattributes *Annotation*

# 保留ServiceLoaderInit类，需要反射调用
-keep class com.sankuai.waimai.router.generated.ServiceLoaderInit { *; }

# 避免注解在shrink阶段就被移除，导致obfuscate阶段注解失效、实现类仍然被混淆
-keep @interface com.sankuai.waimai.router.annotation.RouterService
# 使用了RouterService注解的实现类，需要避免Proguard把构造方法、方法等成员移除(shrink)或混淆(obfuscate)，导致无法反射调用。实现类的类名可以混淆。
-keepclassmembers @com.sankuai.waimai.router.annotation.RouterService class * { *; }

-keepclasseswithmembers, allowoptimization public class * {
    public <methods>;
    public <fields>;
}