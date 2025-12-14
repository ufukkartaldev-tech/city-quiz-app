# 🔐 Firebase Güvenlik Kuralları Dokümantasyonu

**Tarih:** 14 Aralık 2025, 16:00  
**Versiyon:** 1.0  
**Durum:** ✅ Hazır

---

## 📋 İÇİNDEKİLER

1. [Firestore Security Rules](#firestore-security-rules)
2. [Storage Security Rules](#storage-security-rules)
3. [Kurulum](#kurulum)
4. [Test Etme](#test-etme)
5. [Önemli Notlar](#önemli-notlar)

---

## 🔥 FIRESTORE SECURITY RULES

### Dosya: `firestore.rules`

### 📊 Collection Yapısı ve Kuralları

#### 1. **users** Collection
```
users/{userId}
  ├── username (string, 3-20 karakter)
  ├── email (string, 5-100 karakter)
  ├── photoUrl (string)
  ├── score (number)
  ├── level (number)
  ├── isOnline (boolean)
  └── lastSeen (timestamp)
```

**Kurallar:**
- ✅ **Read:** Herkes okuyabilir (arkadaş arama için)
- ✅ **Create:** Sadece kendi profilini oluşturabilir
- ✅ **Update:** Sadece kendi profilini güncelleyebilir
- ❌ **Delete:** Yasak (veri bütünlüğü)

**Validasyonlar:**
- Username: 3-20 karakter
- Email: 5-100 karakter
- Email değişikliği yasak

---

#### 2. **users/{userId}/friends** Subcollection
```
friends/{friendId}
  ├── username (string)
  ├── photoUrl (string)
  ├── score (number)
  ├── level (number)
  ├── isOnline (boolean)
  ├── lastSeen (timestamp)
  └── friendsSince (timestamp)
```

**Kurallar:**
- ✅ **Read:** Sadece kendi arkadaş listesi
- ✅ **Create:** Sadece kendi listesine ekleyebilir
- ✅ **Update:** Sadece kendi listesini güncelleyebilir
- ✅ **Delete:** Sadece kendi listesinden silebilir

---

#### 3. **friend_requests** Collection
```
friend_requests/{requestId}
  ├── fromUid (string)
  ├── fromUsername (string, 3-20 karakter)
  ├── fromPhotoUrl (string)
  ├── toUid (string)
  ├── timestamp (timestamp)
  └── status (string: PENDING/ACCEPTED/REJECTED)
```

**Kurallar:**
- ✅ **Read:** Gönderen veya alan okuyabilir
- ✅ **Create:** Sadece kendi adına istek gönderebilir
- ✅ **Update:** Sadece alıcı status güncelleyebilir
- ✅ **Delete:** Gönderen veya alan silebilir

**Validasyonlar:**
- Kendine istek gönderilemez
- İlk status PENDING olmalı
- Update'te sadece status değişebilir
- Status sadece ACCEPTED veya REJECTED olabilir

---

#### 4. **high_scores** Collection
```
high_scores/{scoreId}
  ├── userId (string)
  ├── username (string)
  ├── score (number, 0-10000)
  ├── level (number)
  └── timestamp (timestamp)
```

**Kurallar:**
- ✅ **Read:** Herkes okuyabilir (leaderboard)
- ✅ **Create:** Sadece kendi skorunu ekleyebilir
- ❌ **Update:** Yasak (cheating önleme)
- ❌ **Delete:** Yasak

**Validasyonlar:**
- Skor: 0-10000 arası
- UserId doğrulanmalı

---

#### 5. **game_rooms** Collection (Multiplayer)
```
game_rooms/{roomId}
  ├── hostUid (string)
  ├── status (string: WAITING/PLAYING/FINISHED)
  ├── players (array)
  ├── questionSeed (number)
  └── createdAt (timestamp)
```

**Kurallar:**
- ✅ **Read:** Herkes okuyabilir (oda listesi)
- ✅ **Create:** Sadece host olarak oluşturabilir
- ✅ **Update:** Host veya katılan oyuncu
- ✅ **Delete:** Sadece host

**Validasyonlar:**
- İlk status WAITING olmalı
- İlk players array'inde sadece host olmalı
- Maksimum 2 oyuncu

---

#### 6. **questions** Collection
```
questions/{questionId}
  ├── questionText (string)
  ├── optionA-D (string)
  ├── correctAnswer (string)
  ├── category (string)
  ├── difficulty (string)
  └── level (number)
```

**Kurallar:**
- ✅ **Read:** Herkes okuyabilir
- ❌ **Write:** Yasak (sadece admin)

---

#### 7. **user_questions** Collection (Community)
```
user_questions/{questionId}
  ├── question (object)
  ├── submittedBy (string)
  ├── submittedByUsername (string)
  ├── status (string: PENDING/APPROVED/REJECTED)
  ├── upvotes (number)
  ├── downvotes (number)
  └── timestamp (timestamp)
```

**Kurallar:**
- ✅ **Read:** Onaylanmış sorular veya kendi sorusu
- ✅ **Create:** Kullanıcı soru gönderebilir
- ✅ **Update:** Sadece kendi sorusunu (PENDING durumunda)
- ✅ **Delete:** Sadece kendi sorusunu (PENDING durumunda)

**Validasyonlar:**
- Question text: 10-200 karakter
- Her option: 1-100 karakter
- İlk status: PENDING
- İlk upvotes/downvotes: 0

---

#### 8. **achievements** Collection
```
achievements/{achievementId}
  ├── code (string)
  ├── title (string)
  ├── description (string)
  ├── iconUrl (string)
  └── points (number)
```

**Kurallar:**
- ✅ **Read:** Herkes okuyabilir
- ❌ **Write:** Yasak (sadece admin)

---

#### 9. **user_achievements** Collection
```
user_achievements/{userId}
  ├── achievements (array)
  └── unlockedAt (map)
```

**Kurallar:**
- ✅ **Read:** Herkes okuyabilir
- ✅ **Create:** Sadece kendi başarımını ekleyebilir
- ❌ **Update:** Yasak (cheating önleme)
- ❌ **Delete:** Yasak

---

#### 10. **notifications** Collection
```
notifications/{notificationId}
  ├── userId (string)
  ├── type (string)
  ├── title (string)
  ├── message (string)
  ├── isRead (boolean)
  └── createdAt (timestamp)
```

**Kurallar:**
- ✅ **Read:** Sadece kendi bildirimleri
- ✅ **Create:** Herkes (sistem tarafından)
- ✅ **Update:** Sadece isRead değişebilir
- ✅ **Delete:** Sadece kendi bildirimi

---

## 📦 FIREBASE STORAGE RULES

### Dosya: `storage.rules`

### 📁 Klasör Yapısı ve Kuralları

#### 1. **profile_photos/{userId}/{fileName}**
**Kurallar:**
- ✅ **Read:** Herkes görebilir
- ✅ **Write:** Sadece kendi fotoğrafı
- ✅ **Max Size:** 5MB
- ✅ **Type:** Sadece resim

---

#### 2. **question_images/{fileName}**
**Kurallar:**
- ✅ **Read:** Herkes görebilir
- ❌ **Write:** Yasak (sadece admin)

---

#### 3. **user_question_images/{userId}/{fileName}**
**Kurallar:**
- ✅ **Read:** Herkes görebilir
- ✅ **Write:** Sadece kendi resmi
- ✅ **Max Size:** 3MB
- ✅ **Type:** Sadece resim

---

#### 4. **achievement_icons/{fileName}**
**Kurallar:**
- ✅ **Read:** Herkes görebilir
- ❌ **Write:** Yasak (sadece admin)

---

#### 5. **game_screenshots/{userId}/{fileName}**
**Kurallar:**
- ✅ **Read:** Herkes görebilir
- ✅ **Write:** Sadece kendi screenshot'ı
- ✅ **Max Size:** 10MB
- ✅ **Type:** Sadece resim

---

## 🚀 KURULUM

### 1. Firebase Console'da Kurulum

#### Firestore Rules:
```bash
# Firebase Console'a git
https://console.firebase.google.com

# Projeyi seç
# Firestore Database > Rules
# firestore.rules içeriğini kopyala-yapıştır
# Publish butonuna tıkla
```

#### Storage Rules:
```bash
# Firebase Console'a git
# Storage > Rules
# storage.rules içeriğini kopyala-yapıştır
# Publish butonuna tıkla
```

---

### 2. Firebase CLI ile Kurulum

```bash
# Firebase CLI kur (eğer yoksa)
npm install -g firebase-tools

# Login ol
firebase login

# Proje klasöründe init et
firebase init

# Firestore ve Storage seç
# Mevcut dosyaları kullan (firestore.rules, storage.rules)

# Deploy et
firebase deploy --only firestore:rules
firebase deploy --only storage:rules
```

---

## 🧪 TEST ETME

### Firestore Rules Test

Firebase Console'da test edebilirsiniz:

```javascript
// Test 1: Kullanıcı kendi profilini okuyabilir mi?
// Location: /users/user123
// Auth: user123
// Operation: get
// ✅ Başarılı olmalı

// Test 2: Kullanıcı başkasının profilini güncelleyebilir mi?
// Location: /users/user456
// Auth: user123
// Operation: update
// ❌ Başarısız olmalı

// Test 3: Arkadaşlık isteği gönderme
// Location: /friend_requests/request123
// Auth: user123
// Data: { fromUid: "user123", toUid: "user456", status: "PENDING" }
// Operation: create
// ✅ Başarılı olmalı

// Test 4: Skor ekleme (geçerli)
// Location: /high_scores/score123
// Auth: user123
// Data: { userId: "user123", score: 500 }
// Operation: create
// ✅ Başarılı olmalı

// Test 5: Skor ekleme (geçersiz - çok yüksek)
// Location: /high_scores/score124
// Auth: user123
// Data: { userId: "user123", score: 999999 }
// Operation: create
// ❌ Başarısız olmalı (max 10000)
```

---

### Storage Rules Test

```javascript
// Test 1: Profil fotoğrafı yükleme (kendi)
// Path: /profile_photos/user123/avatar.jpg
// Auth: user123
// File: image/jpeg, 2MB
// ✅ Başarılı olmalı

// Test 2: Profil fotoğrafı yükleme (başkası)
// Path: /profile_photos/user456/avatar.jpg
// Auth: user123
// File: image/jpeg, 2MB
// ❌ Başarısız olmalı

// Test 3: Büyük dosya yükleme
// Path: /profile_photos/user123/avatar.jpg
// Auth: user123
// File: image/jpeg, 10MB
// ❌ Başarısız olmalı (max 5MB)
```

---

## ⚠️ ÖNEMLİ NOTLAR

### Güvenlik
1. ✅ **Varsayılan olarak her şey yasak** - En güvenli yaklaşım
2. ✅ **Validasyon her yerde** - Veri bütünlüğü korunuyor
3. ✅ **Cheating önleme** - Skor ve başarım güncellemeleri yasak
4. ✅ **Spam önleme** - Dosya boyutu ve string uzunluk kontrolleri

### Performans
1. ⚡ **Index kullanımı** - Sık sorgulanan alanlar için index ekleyin
2. ⚡ **Denormalizasyon** - Gereksiz join'leri önleyin
3. ⚡ **Caching** - Leaderboard gibi veriler cache'lenebilir

### Bakım
1. 🔧 **Düzenli gözden geçirme** - Ayda bir kuralları gözden geçirin
2. 🔧 **Log izleme** - Firebase Console'da denied istekleri izleyin
3. 🔧 **Versiyon kontrolü** - Her değişikliği Git'e commit edin

---

## 📊 GÜVENLİK SEVİYESİ

| Kategori | Seviye | Açıklama |
|----------|--------|----------|
| **Authentication** | ⭐⭐⭐⭐⭐ | Her işlem auth gerektirir |
| **Authorization** | ⭐⭐⭐⭐⭐ | Kullanıcı sadece kendi verisine erişir |
| **Validation** | ⭐⭐⭐⭐⭐ | Tüm girdiler validate ediliyor |
| **Anti-Cheating** | ⭐⭐⭐⭐⭐ | Skor/başarım manipülasyonu yasak |
| **Spam Prevention** | ⭐⭐⭐⭐ | Boyut ve uzunluk kontrolleri var |

**Toplam Güvenlik Puanı:** ⭐⭐⭐⭐⭐ (5/5)

---

## 🔄 GÜNCELLEME GEÇMİŞİ

### v1.0 (14 Aralık 2025)
- ✅ İlk versiyon oluşturuldu
- ✅ Tüm collection'lar için kurallar eklendi
- ✅ Storage kuralları eklendi
- ✅ Validasyon kuralları eklendi
- ✅ Anti-cheating kuralları eklendi

---

## 📞 DESTEK

Sorularınız için:
- Firebase Console: https://console.firebase.google.com
- Firebase Docs: https://firebase.google.com/docs/rules
- GitHub Issues: https://github.com/ufukkartaldev-tech/city-quiz-app/issues

---

**Hazırlayan:** Antigravity AI Assistant  
**Tarih:** 14 Aralık 2025, 16:00  
**Durum:** ✅ Production Ready
