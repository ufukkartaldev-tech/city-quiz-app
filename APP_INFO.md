# 📱 Uygulama Bilgileri

## 🎮 Uygulama Detayları

| Özellik | Değer |
|---------|-------|
| **Uygulama Adı** | Şehir Bilgi Yarışması (OYUN) |
| **Package Name** | `com.example.oyun` |
| **Version Code** | 2 |
| **Version Name** | 1.1 |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 34 (Android 14) |

---

## 🔐 AdMob Bilgileri

### Application ID
```
ca-app-pub-1334433458655438~8781985482
```

### Reklam Birimleri

**Banner Ad:**
```
ca-app-pub-1334433458655438/1398319482
```

**Interstitial Ad:**
```
ca-app-pub-1334433458655438/7748121326
```

**Rewarded Ad:**
```
ca-app-pub-1334433458655438/6975640297
```

### Kullanım Yerleri
- ✅ `AndroidManifest.xml` - Application ID
- ✅ `AdManager.kt` - Tüm reklam birimleri
- ✅ UMP SDK entegre (GDPR uyumluluğu)

---

## 🔒 Gizlilik Politikası

**URL:**
```
https://ufukkartaldev-tech.github.io/privacy-policy/
```

**Durum:** ✅ Yayında ve erişilebilir

**İçerik:**
- Toplanan veriler
- Veri kullanımı
- AdMob ve Analytics bilgileri
- Kullanıcı hakları
- İletişim bilgileri

---

## 🔑 Keystore Bilgileri

**Dosya:** `oyun-release-key.jks`

| Özellik | Değer |
|---------|-------|
| **Store Password** | oyun2024 |
| **Key Password** | oyun2024 |
| **Key Alias** | oyun |

⚠️ **ÖNEMLİ:** Bu bilgileri güvenli bir yerde saklayın!

---

## 🔥 Firebase Bilgileri

**Kullanılan Servisler:**
- ✅ Firebase Authentication (Google Sign-In)
- ✅ Cloud Firestore (Multiplayer, Leaderboard)
- ✅ Firebase Analytics
- ✅ Firebase Crashlytics

**Package Name:** `com.example.oyun`

**Yapılandırma:** `app/google-services.json`

---

## 📊 Özellikler

### Oyun Modları
- 🎯 Tek Oyunculu (10 seviye)
- 👥 Multiplayer (Gerçek zamanlı)
- 🃏 Joker Kazanma Modu

### Joker Sistemi
- **50-50:** İki yanlış şıkkı kaldır
- **Atla:** Soruyu atla
- **Can Kazan:** Şehir sorusu ile can kazan

### Başarım Sistemi
- 10 farklı rozet
- Günlük görevler
- Liderlik tablosu

### Sosyal Özellikler
- Google hesabı ile giriş
- Profil sistemi
- Bulut kayıt
- Skor paylaşımı

---

## 🎨 Store Assets

**Konum:** `play_store_assets/`

| Asset | Boyut | Durum |
|-------|-------|-------|
| App Icon | 512x512 px | ✅ Hazır |
| Feature Graphic | 1024x500 px | ✅ Hazır |
| Screenshots | 1080x1920 px | ✅ Hazır (8 adet) |

---

## 📝 Store Listing Bilgileri

### Kategori
**Önerilen:** Trivia / Education

### Hedef Kitle
**Yaş:** 13+  
**Çocuklara yönelik değil**

### Kısa Açıklama
```
Türkiye'nin şehirleri hakkında bilginizi test edin! Çok oyunculu mod ile yarışın!
```

### Anahtar Kelimeler
- Bilgi yarışması
- Quiz
- Türkiye
- Şehirler
- Multiplayer
- Eğitici oyun
- Trivia

---

## 🔧 Teknik Detaylar

### Kullanılan Kütüphaneler

**Core:**
- Kotlin 1.9.22
- AndroidX Core KTX 1.12.0
- Material Design 1.11.0

**Architecture:**
- ViewModel & LiveData
- Hilt (Dependency Injection)
- Room Database 2.6.1
- Coroutines & Flow

**Firebase:**
- Firebase BOM 33.6.0
- Auth, Firestore, Analytics, Crashlytics

**Ads:**
- Google Play Services Ads 22.6.0
- UMP SDK (User Messaging Platform)

### Build Yapılandırması

**Debug:**
```bash
.\gradlew.bat assembleDebug
```

**Release:**
```bash
.\gradlew.bat bundleRelease
```

**Çıktı:**
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`

---

## ✅ Hazırlık Durumu

### Tamamlanan
- ✅ Kod refactoring
- ✅ Build başarılı
- ✅ AdMob entegrasyonu
- ✅ Firebase entegrasyonu
- ✅ Gizlilik politikası yayında
- ✅ Store assets hazır
- ✅ Keystore oluşturuldu

### Yapılacaklar
- [ ] AdMob GDPR mesajı yayınla
- [ ] Release AAB oluştur
- [ ] Play Console'da uygulama oluştur
- [ ] Store listing doldur
- [ ] İncelemeye gönder

---

## 📞 İletişim

**Destek E-posta:** destek@sehirbilgiyarismasi.com  
**Gizlilik Politikası:** https://ufukkartaldev-tech.github.io/privacy-policy/  
**GitHub:** https://github.com/ufukkartaldev-tech/

---

**Son Güncelleme:** 9 Aralık 2025, 20:35
