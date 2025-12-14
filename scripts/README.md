# 📝 Soru Oluşturma ve Yönetim Script'leri

Bu klasör, quiz uygulaması için soru oluşturma, düzenleme ve Firestore'a yükleme script'lerini içerir.

---

## 📁 Dosyalar

### 1. `question_generator.py`
**Manuel soru oluşturma script'i**

Şablonlardan ve varyasyonlardan soru oluşturur.

**Kullanım:**
```bash
python question_generator.py
```

**Özellikler:**
- ✅ 7 kategori (Coğrafya, Tarih, Kültür, Spor, Genel, Bilim, Sanat)
- ✅ 4 zorluk seviyesi (Easy, Medium, Hard, Expert)
- ✅ 10 level desteği
- ✅ JSON export (Android ve Firestore için)
- ✅ İstatistik gösterimi

**Çıktı Dosyaları:**
- `questions_extended.json` - Android için
- `firestore_import.json` - Firestore için

---

### 2. `firebase_upload.py`
**Firestore'a soru yükleme script'i**

Soruları Firestore'a batch işlemiyle yükler.

**Kurulum:**
```bash
pip install firebase-admin
```

**Kullanım:**
```bash
python firebase_upload.py
```

**Özellikler:**
- ✅ Batch upload (500 soru/batch)
- ✅ İstatistik gösterimi
- ✅ Soru sorgulama (kategori, zorluk, level)
- ✅ Toplu silme
- ✅ Güncelleme

**Gereksinimler:**
- `serviceAccountKey.json` dosyası (Firebase Console'dan indir)

**serviceAccountKey.json Nasıl Alınır:**
1. Firebase Console > Project Settings
2. Service Accounts sekmesi
3. "Generate New Private Key" butonuna tıkla
4. İndirilen dosyayı `serviceAccountKey.json` olarak kaydet

---

### 3. `ai_question_generator.py`
**AI ile soru oluşturma script'i**

OpenAI GPT-4 veya Google Gemini kullanarak otomatik soru oluşturur.

**Kurulum:**
```bash
# OpenAI için
pip install openai

# Gemini için
pip install google-generativeai
```

**Kullanım:**
```bash
# API key'leri ayarla
export OPENAI_API_KEY="your-openai-key"
# veya
export GEMINI_API_KEY="your-gemini-key"

# Script'i çalıştır
python ai_question_generator.py
```

**Özellikler:**
- ✅ OpenAI GPT-4 desteği
- ✅ Google Gemini desteği (ücretsiz)
- ✅ Otomatik soru oluşturma
- ✅ Kategori ve zorluk bazlı üretim
- ✅ JSON export

**Maliyet:**
- OpenAI: ~$0.03 per 1000 tokens (ücretli)
- Gemini: Ücretsiz (günlük limit var)

---

## 🚀 Hızlı Başlangıç

### Adım 1: Manuel Soru Oluşturma
```bash
# 500 soru oluştur
python question_generator.py
# Çıktı: questions_extended.json
```

### Adım 2: Firestore'a Yükleme
```bash
# serviceAccountKey.json dosyasını hazırla
# Soruları yükle
python firebase_upload.py
# Menüden "1" seç
```

### Adım 3: AI ile Daha Fazla Soru (Opsiyonel)
```bash
# API key ayarla
export GEMINI_API_KEY="your-key"

# 100 soru oluştur
python ai_question_generator.py
# Çıktı: ai_questions_gemini_100.json
```

---

## 📊 Soru Formatı

```json
{
  "id": 1,
  "questionText": "Türkiye'nin başkenti neresidir?",
  "optionA": "İstanbul",
  "optionB": "Ankara",
  "optionC": "İzmir",
  "optionD": "Bursa",
  "correctAnswer": "B",
  "imageName": "geography_1.png",
  "level": 1,
  "category": "GEOGRAPHY",
  "difficulty": "EASY",
  "explanation": "Ankara, 1923'ten beri Türkiye'nin başkentidir.",
  "tags": ["ankara", "başkent"],
  "points": 5,
  "timeLimit": 30,
  "isVerified": true,
  "authorId": "system",
  "createdAt": 1702569600000
}
```

---

## 📚 Kategoriler

| Kod | Türkçe | Açıklama |
|-----|--------|----------|
| `GEOGRAPHY` | Coğrafya | Şehirler, göller, nehirler |
| `HISTORY` | Tarih | Osmanlı, Cumhuriyet dönemi |
| `CULTURE` | Kültür | Gelenekler, UNESCO mirası |
| `SPORTS` | Spor | Futbol, olimpiyatlar |
| `GENERAL` | Genel Kültür | Çeşitli konular |
| `SCIENCE` | Bilim | Bilimsel konular |
| `ART` | Sanat | Müzik, edebiyat, sinema |

---

## ⭐ Zorluk Seviyeleri

| Seviye | Puan | Süre | Açıklama |
|--------|------|------|----------|
| `EASY` | 5 | 30s | Kolay sorular |
| `MEDIUM` | 10 | 25s | Orta zorluk |
| `HARD` | 15 | 20s | Zor sorular |
| `EXPERT` | 25 | 15s | Uzman seviyesi |

---

## 🔧 Gereksinimler

### Python Paketleri
```bash
pip install firebase-admin  # Firestore için
pip install openai          # OpenAI için (opsiyonel)
pip install google-generativeai  # Gemini için (opsiyonel)
```

### Dosyalar
- `serviceAccountKey.json` - Firebase Admin SDK key
- `questions_extended.json` - Oluşturulan sorular

---

## 📝 Örnek Kullanım Senaryoları

### Senaryo 1: İlk Kurulum (500 Soru)
```bash
# 1. Manuel soru oluştur
python question_generator.py
# Soru sayısı: 500

# 2. Firestore'a yükle
python firebase_upload.py
# Menü: 1 (Soruları Yükle)
```

### Senaryo 2: AI ile Genişletme (1000 Soru)
```bash
# 1. Gemini API key al (ücretsiz)
export GEMINI_API_KEY="your-key"

# 2. AI ile 500 soru daha oluştur
python ai_question_generator.py
# Soru sayısı: 500

# 3. Firestore'a yükle
python firebase_upload.py
# Menü: 1, Dosya: ai_questions_gemini_500.json
```

### Senaryo 3: Belirli Kategori Güncelleme
```bash
# 1. Mevcut soruları sorgula
python firebase_upload.py
# Menü: 3 (Soruları Sorgula)
# Kategori: GEOGRAPHY

# 2. Yeni sorular oluştur
python ai_question_generator.py
# Sadece GEOGRAPHY kategorisi için

# 3. Yükle
python firebase_upload.py
```

---

## ⚠️ Önemli Notlar

### Güvenlik
- ✅ `serviceAccountKey.json` dosyasını **ASLA** Git'e eklemeyin!
- ✅ API key'leri environment variable olarak saklayın
- ✅ `.gitignore` dosyasında bu dosyalar listelenmiş

### Performans
- ✅ Firestore batch limiti: 500 işlem
- ✅ AI rate limit: Provider'a göre değişir
- ✅ Büyük yüklemeler için batch kullanın

### Maliyet
- ✅ Gemini: Ücretsiz (günlük limit var)
- ✅ OpenAI: ~$0.03/1000 token
- ✅ Firestore: Ücretsiz quota (50K okuma/gün)

---

## 🐛 Sorun Giderme

### "Firebase Admin SDK yüklü değil"
```bash
pip install firebase-admin
```

### "serviceAccountKey.json bulunamadı"
1. Firebase Console > Project Settings > Service Accounts
2. "Generate New Private Key" tıkla
3. Dosyayı `scripts/` klasörüne kaydet

### "API key bulunamadı"
```bash
# Linux/Mac
export GEMINI_API_KEY="your-key"

# Windows
set GEMINI_API_KEY=your-key
```

### "Firestore permission denied"
- Firebase Console > Firestore > Rules
- `firestore.rules` dosyasını deploy edin

---

## 📞 Destek

Sorularınız için:
- GitHub Issues: https://github.com/ufukkartaldev-tech/city-quiz-app/issues
- Firebase Docs: https://firebase.google.com/docs

---

**Hazırlayan:** Antigravity AI Assistant  
**Tarih:** 14 Aralık 2025  
**Versiyon:** 1.0
