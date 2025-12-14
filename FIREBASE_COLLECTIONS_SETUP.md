# 🔥 Firebase Firestore Koleksiyonları - Kurulum Rehberi

**Tarih:** 14 Aralık 2025, 20:05  
**Durum:** 📋 Planlama

---

## 📊 MEVCUT DURUM

### ✅ Var Olan Koleksiyonlar

**highscores** ✅
- Doküman sayısı: ~15+
- Örnek doküman görüldü:
  - `correctAnswers: 6`
  - `level: 2`
  - `score: 60`
  - `timestamp: 1765363486782`
  - `totalQuestions: 6`
  - `userName: "Misafir"`

---

## 🎯 OLUŞTURULMASI GEREKEN KOLEKSİYONLAR

### 1. users (Kullanıcılar)
**Amaç:** Kullanıcı profilleri ve istatistikleri

**Yapı:**
```javascript
{
  uid: "user_123",
  username: "oyuncu1",
  email: "user@example.com",
  photoUrl: "https://...",
  score: 1500,
  level: 5,
  totalGamesPlayed: 50,
  totalCorrectAnswers: 200,
  totalWrongAnswers: 50,
  achievements: ["first_win", "level_5"],
  createdAt: Timestamp,
  lastSeen: Timestamp,
  isOnline: false,
  // Joker sayıları
  jokers: {
    fifty_fifty: 3,
    skip_question: 2,
    extra_life: 1
  }
}
```

---

### 2. friend_requests (Arkadaşlık İstekleri)
**Amaç:** Arkadaşlık sistemi

**Yapı:**
```javascript
{
  id: "request_123",
  fromUid: "user_123",
  fromUsername: "oyuncu1",
  fromPhotoUrl: "https://...",
  toUid: "user_456",
  toUsername: "oyuncu2",
  status: "PENDING", // PENDING, ACCEPTED, REJECTED
  createdAt: Timestamp,
  respondedAt: Timestamp
}
```

**Index:**
- `fromUid` + `status`
- `toUid` + `status`

---

### 3. game_rooms (Çok Oyunculu Odalar)
**Amaç:** Real-time multiplayer oyunlar

**Yapı:**
```javascript
{
  id: "room_123",
  hostUid: "user_123",
  hostUsername: "oyuncu1",
  guestUid: "user_456",
  guestUsername: "oyuncu2",
  status: "WAITING", // WAITING, PLAYING, FINISHED
  currentQuestionIndex: 0,
  hostScore: 0,
  guestScore: 0,
  questions: [
    {
      id: 1,
      questionText: "...",
      options: ["A", "B", "C", "D"],
      correctAnswer: "A"
    }
  ],
  hostAnswers: [],
  guestAnswers: [],
  createdAt: Timestamp,
  startedAt: Timestamp,
  finishedAt: Timestamp
}
```

**Index:**
- `status`
- `hostUid`
- `guestUid`

---

### 4. questions (Sorular - Firestore)
**Amaç:** Level 20+ için cloud sorular

**Yapı:**
```javascript
{
  id: "q_123",
  questionText: "Türkiye'nin başkenti neresidir?",
  optionA: "İstanbul",
  optionB: "Ankara",
  optionC: "İzmir",
  optionD: "Bursa",
  correctAnswer: "B",
  imageName: "ankara.png",
  level: 20,
  category: "GEOGRAPHY",
  difficulty: "EASY", // EASY, MEDIUM, HARD, EXPERT
  explanation: "Türkiye'nin başkenti 1923'ten beri Ankara'dır.",
  tags: ["başkent", "coğrafya", "türkiye"],
  points: 10,
  timeLimit: 30,
  isVerified: true,
  authorId: "system",
  createdAt: Timestamp,
  updatedAt: Timestamp
}
```

**Index:**
- `level`
- `category`
- `difficulty`
- `isVerified`

---

### 5. user_questions (Kullanıcı Soruları)
**Amaç:** Topluluk tarafından oluşturulan sorular

**Yapı:**
```javascript
{
  id: "uq_123",
  questionText: "...",
  optionA: "...",
  optionB: "...",
  optionC: "...",
  optionD: "...",
  correctAnswer: "A",
  category: "GENERAL",
  difficulty: "MEDIUM",
  authorId: "user_123",
  authorUsername: "oyuncu1",
  status: "PENDING", // PENDING, APPROVED, REJECTED
  upvotes: 10,
  downvotes: 2,
  reports: 0,
  createdAt: Timestamp,
  reviewedAt: Timestamp,
  reviewedBy: "admin_123"
}
```

**Index:**
- `status`
- `authorId`
- `category`

---

### 6. achievements (Başarımlar)
**Amaç:** Oyun başarımları tanımları

**Yapı:**
```javascript
{
  id: "ach_first_win",
  code: "first_win",
  title: "İlk Zafer",
  description: "İlk oyununu kazan",
  icon: "trophy_gold.png",
  points: 50,
  category: "GAMEPLAY",
  rarity: "COMMON", // COMMON, RARE, EPIC, LEGENDARY
  requirement: {
    type: "WIN_GAMES",
    count: 1
  },
  createdAt: Timestamp
}
```

---

### 7. user_achievements (Kullanıcı Başarımları)
**Amaç:** Kullanıcıların kazandığı başarımlar

**Yapı:**
```javascript
{
  userId: "user_123",
  achievementId: "ach_first_win",
  unlockedAt: Timestamp,
  progress: 100, // 0-100
  notified: true
}
```

**Composite ID:** `userId_achievementId`

**Index:**
- `userId`
- `achievementId`

---

### 8. notifications (Bildirimler)
**Amaç:** Push notifications ve in-app bildirimler

**Yapı:**
```javascript
{
  id: "notif_123",
  userId: "user_123",
  type: "FRIEND_REQUEST", // FRIEND_REQUEST, GAME_INVITE, ACHIEVEMENT, LEVEL_UP
  title: "Yeni arkadaşlık isteği",
  message: "oyuncu2 seni arkadaş olarak ekledi",
  data: {
    fromUserId: "user_456",
    requestId: "request_123"
  },
  read: false,
  createdAt: Timestamp,
  readAt: Timestamp
}
```

**Index:**
- `userId` + `read`
- `createdAt`

---

### 9. daily_challenges (Günlük Görevler)
**Amaç:** Her gün yenilenen görevler

**Yapı:**
```javascript
{
  id: "challenge_20251214",
  date: "2025-12-14",
  challenges: [
    {
      id: "ch_1",
      title: "5 Oyun Kazan",
      description: "Bugün 5 oyun kazan",
      type: "WIN_GAMES",
      target: 5,
      reward: {
        type: "JOKER",
        jokerType: "fifty_fifty",
        amount: 2
      }
    },
    {
      id: "ch_2",
      title: "Mükemmel Skor",
      description: "Bir oyunda tüm soruları doğru cevapla",
      type: "PERFECT_SCORE",
      target: 1,
      reward: {
        type: "POINTS",
        amount: 100
      }
    }
  ],
  createdAt: Timestamp,
  expiresAt: Timestamp
}
```

---

### 10. user_daily_progress (Kullanıcı Günlük İlerleme)
**Amaç:** Günlük görev ilerlemesi

**Yapı:**
```javascript
{
  userId: "user_123",
  date: "2025-12-14",
  challenges: {
    "ch_1": {
      progress: 3,
      target: 5,
      completed: false,
      claimedReward: false
    },
    "ch_2": {
      progress: 0,
      target: 1,
      completed: false,
      claimedReward: false
    }
  },
  updatedAt: Timestamp
}
```

**Composite ID:** `userId_date`

---

## 🔒 GÜVENLİK KURALLARI

### firestore.rules (Zaten Mevcut)

Güvenlik kuralları zaten `firestore.rules` dosyasında tanımlı:
- ✅ users koleksiyonu
- ✅ friend_requests koleksiyonu
- ✅ highscores koleksiyonu
- ✅ game_rooms koleksiyonu
- ✅ questions koleksiyonu
- ✅ achievements koleksiyonu
- ✅ notifications koleksiyonu

**Dosya:** `firestore.rules` (211 satır)

---

## 📝 KURULUM ADIMLARI

### Yöntem 1: Firebase Console (Manuel)

1. **Firebase Console:** https://console.firebase.google.com
2. **Firestore Database** > **Data** sekmesi
3. Her koleksiyon için:
   - **Start collection**
   - Collection ID gir
   - İlk dokümanı ekle
   - **Save**

### Yöntem 2: Python Script (Otomatik)

`scripts/firebase_upload.py` kullanarak:

```bash
cd scripts
python firebase_upload.py
```

**Seçenekler:**
1. Upload questions to Firestore
2. Create sample users
3. Create sample achievements
4. Initialize collections

---

## 🎯 ÖNCELİK SIRASI

### Hemen Yapılacaklar (Kritik)

1. ✅ **highscores** - Zaten var
2. ⏳ **users** - Kullanıcı sistemi için gerekli
3. ⏳ **questions** - Level 20+ sorular için

### Önemli (Yakında)

4. ⏳ **friend_requests** - Sosyal özellikler
5. ⏳ **game_rooms** - Multiplayer
6. ⏳ **achievements** - Başarım sistemi

### Opsiyonel (Sonra)

7. ⏳ **user_questions** - Topluluk soruları
8. ⏳ **notifications** - Bildirimler
9. ⏳ **daily_challenges** - Günlük görevler
10. ⏳ **user_daily_progress** - Görev takibi

---

## 🚀 HIZLI BAŞLANGIÇ

### 1. users Koleksiyonu Oluştur

**Firebase Console:**
1. Firestore Database > Start collection
2. Collection ID: `users`
3. İlk doküman:
   ```
   Document ID: test_user_123
   Fields:
   - uid: "test_user_123"
   - username: "TestUser"
   - email: "test@example.com"
   - score: 0
   - level: 1
   - createdAt: (Timestamp - now)
   ```

### 2. questions Koleksiyonu Oluştur

**Python Script ile:**
```bash
cd scripts
python question_generator.py
python firebase_upload.py
```

### 3. Güvenlik Kurallarını Deploy Et

```bash
firebase deploy --only firestore:rules
```

---

## 📊 SONUÇ

**Mevcut:** 1 koleksiyon (highscores) ✅  
**Hedef:** 10 koleksiyon  
**Öncelik:** users, questions, friend_requests

**Sonraki Adım:** Hangi koleksiyonu oluşturmak istersiniz?

---

**Hazırlayan:** Antigravity AI Assistant  
**Tarih:** 14 Aralık 2025, 20:05  
**Durum:** 📋 Plan Hazır - Kurulum Bekliyor
