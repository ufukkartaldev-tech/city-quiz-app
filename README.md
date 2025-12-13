# 🎮 Quiz Oyunu - Android Bilgi Yarışması

Modern ve eğlenceli bir Android bilgi yarışması uygulaması. Material Design 3, MVVM mimarisi ve Kotlin ile geliştirilmiştir.

## 📱 Özellikler

### 🎯 Oyun Modları
- **Tek Oyunculu Mod**: 10 seviye, her seviyede 10 soru
- **Çok Oyunculu Mod**: Gerçek zamanlı rakiple yarış
- **Joker Sistemi**: 50-50, Soru Atla, Can Kazan

### 🏆 Özellikler
- ✅ Material Design 3 tasarım
- ✅ Dark Mode desteği
- ✅ Çoklu dil desteği (Türkçe/İngilizce)
- ✅ Firebase entegrasyonu
- ✅ Google Sign-In
- ✅ Yüksek skor sistemi
- ✅ Başarım sistemi
- ✅ Günlük görevler
- ✅ Reklam entegrasyonu (AdMob)
- ✅ Ses efektleri
- ✅ Smooth animasyonlar

## 🛠️ Teknolojiler

### Mimari & Pattern
- **MVVM** (Model-View-ViewModel)
- **Clean Architecture**
- **Repository Pattern**
- **Dependency Injection** (Hilt)

### Kütüphaneler
```gradle
// Core
- Kotlin
- Coroutines & Flow
- ViewModel & LiveData

// UI
- Material Design 3
- View Binding
- RecyclerView
- CardView

// Dependency Injection
- Hilt (Dagger)

// Database
- Room Database

// Network & Auth
- Firebase Auth
- Firebase Firestore
- Firebase Analytics
- Google Sign-In

// Ads
- Google AdMob

// Image Loading
- Glide
```

## 📂 Proje Yapısı

```
app/src/main/java/com/example/oyun/
├── data/               # Veri katmanı
│   ├── local/         # Room Database
│   ├── remote/        # Firebase, API
│   └── repository/    # Repository'ler
├── domain/            # İş mantığı
│   └── GameViewModel.kt
├── ui/                # Sunum katmanı
│   ├── activities/    # Activity'ler
│   ├── adapters/      # RecyclerView Adapter'ları
│   ├── game/          # Oyun UI bileşenleri
│   ├── main/          # Ana ekran bileşenleri
│   └── multiplayer/   # Multiplayer UI
├── managers/          # Yardımcı yöneticiler
│   ├── SoundManager.kt
│   ├── TimeManager.kt
│   └── UIManager.kt
├── di/                # Dependency Injection
│   └── AppModule.kt
└── utils/             # Yardımcı fonksiyonlar
    └── ViewExtensions.kt
```

## 🚀 Kurulum

### Gereksinimler
- Android Studio Hedgehog | 2023.1.1 veya üzeri
- JDK 17
- Android SDK 34
- Gradle 8.2

### Adımlar

1. **Projeyi Klonla**
```bash
git clone https://github.com/kullaniciadi/oyun.git
cd oyun
```

2. **Firebase Kurulumu**
   - Firebase Console'da yeni proje oluştur
   - Android uygulaması ekle (package name: `com.example.oyun`)
   - `google-services.json` dosyasını indir
   - `app/` klasörüne kopyala

3. **API Keys Ayarla**
   - `local.properties` dosyası oluştur (proje root'unda)
   - Gerekli API key'leri ekle:
   ```properties
   # AdMob
   ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
   ADMOB_BANNER_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
   ADMOB_INTERSTITIAL_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
   ADMOB_REWARDED_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
   ```

4. **Keystore Oluştur** (Release için)
```bash
keytool -genkey -v -keystore oyun-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias oyun
```

5. **Build Et**
```bash
./gradlew assembleDebug
```

## 🔐 Güvenlik

### Hassas Bilgiler
Aşağıdaki dosyalar **asla** Git'e eklenmemelidir:

- ❌ `google-services.json` (Firebase config)
- ❌ `keystore.properties` (Signing config)
- ❌ `*.jks`, `*.keystore` (Keystore dosyaları)
- ❌ `local.properties` (API keys)

Bu dosyalar `.gitignore` ile korunmaktadır.

### API Keys Yönetimi

**Geliştirme için:**
- `local.properties` dosyasında sakla
- Gradle'dan oku:
```gradle
def localProperties = new Properties()
localProperties.load(new FileInputStream(rootProject.file("local.properties")))
```

**Production için:**
- GitHub Secrets kullan
- CI/CD pipeline'da inject et

## 🎨 Tasarım

### Renk Paleti
- **Primary**: #0057B7 (Mavi)
- **Secondary**: #FF6B35 (Turuncu)
- **Success**: #4CAF50 (Yeşil)
- **Error**: #F44336 (Kırmızı)

### Animasyonlar
- Scale down/up (Buton tıklama)
- Shake (Yanlış cevap)
- Pulse (Başarı)
- Fade in/out (Geçişler)

## 📝 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

## 👨‍💻 Geliştirici

**[Adınız]**
- GitHub: [@kullaniciadi](https://github.com/kullaniciadi)
- Email: email@example.com

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'feat: Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request açın

## 📸 Ekran Görüntüleri

*(Ekran görüntülerini buraya ekleyin)*

## 🐛 Bilinen Sorunlar

Şu an bilinen bir sorun bulunmamaktadır.

## 📋 Yapılacaklar

- [ ] Lottie animasyonları
- [ ] Haptic feedback
- [ ] Custom fontlar
- [ ] Daha fazla soru kategorisi
- [ ] Leaderboard sistemi

## 📞 İletişim

Sorularınız için issue açabilir veya email gönderebilirsiniz.

---

**⭐ Projeyi beğendiyseniz yıldız vermeyi unutmayın!**
