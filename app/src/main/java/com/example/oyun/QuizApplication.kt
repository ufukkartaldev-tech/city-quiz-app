package com.example.oyun

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Bu sınıf, uygulamanın ana giriş noktasıdır. Hilt'in çalışabilmesi için ZORUNLUDUR.
 * Tepesindeki @HiltAndroidApp etiketi, Hilt'e "Bütün sihrini burada başlat" komutunu verir.
 * Bu etiket olmadan, hiçbir @Inject, @Module veya @HiltViewModel çalışmaz.
 * * Bu sınıfın adını AndroidManifest.xml dosyasında <application> etiketinin içinde
 * android:name=".QuizApplication" olarak belirtmeyi UNUTMA!
 */
@HiltAndroidApp
class QuizApplication : Application() {

}