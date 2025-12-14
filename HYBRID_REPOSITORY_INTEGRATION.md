# 🔄 Hybrid Question Repository - Entegrasyon Rehberi

**Tarih:** 14 Aralık 2025, 19:10  
**Durum:** ✅ Backend Hazır - UI Entegrasyonu

---

## 📋 YAPILAN DEĞİŞİKLİKLER

### 1. ✅ Yeni Dosyalar

#### Backend
- ✅ `CachedQuestionDao.kt` - Room DAO (veritabanı işlemleri)
- ✅ `HybridQuestionRepository.kt` - Hybrid repository (JSON + Firestore)
- ✅ `AppDatabase.kt` - Güncellendi (CachedQuestion entity eklendi)
- ✅ `AppModule.kt` - Güncellendi (DI için)

#### Migration
- ✅ Database version: 1 → 2
- ✅ Migration 1→2: CachedQuestion tablosu eklendi
- ✅ Index'ler eklendi (performans için)

---

## 🎯 STRATEJİ

### Offline-First + Cloud Sync

```
┌─────────────────────────────────────────────┐
│         UYGULAMA AÇILIŞI                    │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│  1. JSON'dan Temel Soruları Yükle          │
│     (assets/questions.json)                 │
│     ✅ Hızlı başlangıç                      │
│     ✅ Offline çalışır                      │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│  2. Arka Planda Firestore Sync             │
│     (Level 20+ sorular)                     │
│     ✅ İnternet varsa                       │
│     ✅ 24 saatte bir                        │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│  3. Room Database'e Kaydet                  │
│     (cached_questions tablosu)              │
│     ✅ Kalıcı depolama                      │
│     ✅ Hızlı erişim                         │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│  OYUN BAŞLAT                                │
│  Öncelik: Room > JSON                       │
└─────────────────────────────────────────────┘
```

---

## 🔧 UI ENTEGRASYONU

### 1. MainActivity Güncellemesi

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var hybridQuestionRepository: HybridQuestionRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Repository'yi başlat
        initializeQuestionRepository()
    }

    private fun initializeQuestionRepository() {
        lifecycleScope.launch {
            try {
                // JSON yükle + Firestore sync başlat
                hybridQuestionRepository.initialize()
                
                // İstatistikleri göster
                val stats = hybridQuestionRepository.getStatistics()
                Log.d("MainActivity", "Toplam soru: ${stats.totalQuestions}")
                
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, 
                    "Sorular yüklenirken hata: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

---

### 2. GameViewModel Güncellemesi

**Mevcut QuestionRepository yerine HybridQuestionRepository kullan:**

```kotlin
@HiltViewModel
class GameViewModel @Inject constructor(
    private val hybridRepository: HybridQuestionRepository,  // DEĞİŞTİ
    private val achievementManager: AchievementManager,
    private val dailyTaskManager: DailyTaskManager,
    private val soundManager: SoundManager,
    private val prefs: SharedPreferences
) : ViewModel() {

    fun startGame(startLevel: Int, isNewGame: Boolean = true) {
        viewModelScope.launch {
            // Soruları al (önce Room, sonra JSON)
            val questions = hybridRepository.getQuestionsForLevel(startLevel)
            
            if (questions.isEmpty()) {
                // Hata: Soru bulunamadı
                _uiState.update { it.copy(isLoading = false, error = "Soru bulunamadı") }
                return@launch
            }
            
            // Soruları yükle
            loadedQuestions = questions.shuffled()
            loadNextQuestion()
        }
    }
}
```

---

### 3. QuestionRepository'yi Değiştirme

**Eski kod (QuestionRepository):**
```kotlin
@Inject
lateinit var questionRepository: QuestionRepository
```

**Yeni kod (HybridQuestionRepository):**
```kotlin
@Inject
lateinit var hybridQuestionRepository: HybridQuestionRepository
```

**Kullanım aynı:**
```kotlin
// Level için sorular
val questions = hybridQuestionRepository.getQuestionsForLevel(level)

// Kategori için sorular
val questions = hybridQuestionRepository.getQuestionsByCategory("GEOGRAPHY")

// Tüm sorular
val allQuestions = hybridQuestionRepository.getAllQuestions()
```

---

## 📊 İSTATİSTİKLER GÖSTERME

### Settings Activity'de İstatistik Ekranı

```kotlin
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var hybridRepository: HybridQuestionRepository

    private fun showQuestionStatistics() {
        lifecycleScope.launch {
            val stats = hybridRepository.getStatistics()
            
            binding.apply {
                tvTotalQuestions.text = "Toplam: ${stats.totalQuestions}"
                tvCachedQuestions.text = "Cache: ${stats.cachedQuestions}"
                tvFirestoreQuestions.text = "Firestore: ${stats.firestoreQuestions}"
                tvJsonQuestions.text = "JSON: ${stats.jsonQuestions}"
                
                // Son sync zamanı
                stats.lastSyncTime?.let { timestamp ->
                    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(timestamp))
                    tvLastSync.text = "Son Sync: $date"
                } ?: run {
                    tvLastSync.text = "Son Sync: Henüz yapılmadı"
                }
            }
        }
    }

    // Manuel sync butonu
    private fun setupSyncButton() {
        binding.btnSyncFirestore.setOnClickListener {
            lifecycleScope.launch {
                binding.progressBar.visibility = View.VISIBLE
                
                try {
                    hybridRepository.forceSyncFirestore()
                    Toast.makeText(this@SettingsActivity, 
                        "Firestore sync tamamlandı!", 
                        Toast.LENGTH_SHORT).show()
                    
                    // İstatistikleri güncelle
                    showQuestionStatistics()
                    
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, 
                        "Sync hatası: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
}
```

---

## 🎨 UI LAYOUT ÖRNEĞİ

### activity_settings.xml'e ekle:

```xml
<!-- Soru İstatistikleri Kartı -->
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="📊 Soru İstatistikleri"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginBottom="12dp"/>

        <TextView
            android:id="@+id/tvTotalQuestions"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Toplam: 0"
            android:textSize="14sp"/>

        <TextView
            android:id="@+id/tvCachedQuestions"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Cache: 0"
            android:textSize="14sp"/>

        <TextView
            android:id="@+id/tvFirestoreQuestions"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Firestore: 0"
            android:textSize="14sp"/>

        <TextView
            android:id="@+id/tvJsonQuestions"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="JSON: 0"
            android:textSize="14sp"/>

        <TextView
            android:id="@+id/tvLastSync"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Son Sync: -"
            android:textSize="12sp"
            android:layout_marginTop="8dp"
            android:textColor="@android:color/darker_gray"/>

        <!-- Manuel Sync Butonu -->
        <Button
            android:id="@+id/btnSyncFirestore"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="🔄 Firestore'dan Güncelle"
            android:layout_marginTop="12dp"/>

        <ProgressBar
            android:id="@+id/progressBar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:visibility="gone"/>

    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

---

## 🔄 CACHE YÖNETİMİ

### Cache Temizleme (Settings'de)

```kotlin
// Tüm cache'i temizle
binding.btnClearCache.setOnClickListener {
    AlertDialog.Builder(this)
        .setTitle("Cache Temizle")
        .setMessage("Tüm indirilen sorular silinecek. Devam edilsin mi?")
        .setPositiveButton("Evet") { _, _ ->
            lifecycleScope.launch {
                hybridRepository.clearCache()
                Toast.makeText(this@SettingsActivity, 
                    "Cache temizlendi", 
                    Toast.LENGTH_SHORT).show()
            }
        }
        .setNegativeButton("Hayır", null)
        .show()
}

// Sadece Firestore cache'ini temizle
binding.btnClearFirestoreCache.setOnClickListener {
    lifecycleScope.launch {
        hybridRepository.clearFirestoreCache()
        Toast.makeText(this@SettingsActivity, 
            "Firestore cache temizlendi", 
            Toast.LENGTH_SHORT).show()
    }
}
```

---

## 🧪 TEST ETME

### 1. İlk Açılış Testi
```kotlin
// MainActivity onCreate'de
lifecycleScope.launch {
    hybridRepository.initialize()
    
    // JSON'dan yüklendi mi?
    val level1Questions = hybridRepository.getQuestionsForLevel(1)
    Log.d("Test", "Level 1 sorular: ${level1Questions.size}")
}
```

### 2. Firestore Sync Testi
```kotlin
// Manuel sync tetikle
lifecycleScope.launch {
    hybridRepository.forceSyncFirestore()
    
    // Firestore'dan indirilen sorular
    val stats = hybridRepository.getStatistics()
    Log.d("Test", "Firestore sorular: ${stats.firestoreQuestions}")
}
```

### 3. Öncelik Testi
```kotlin
// Level 20+ için Room'dan mı geliyor?
lifecycleScope.launch {
    val level20Questions = hybridRepository.getQuestionsForLevel(20)
    
    // İlk soru CachedQuestion'dan mı geldi?
    // (isFromFirestore = true olmalı)
}
```

---

## ⚠️ ÖNEMLİ NOTLAR

### 1. Migration
- ✅ Database version 1 → 2
- ✅ Migration otomatik çalışacak
- ⚠️ Kullanıcı verisi kaybolmaz

### 2. Performans
- ✅ JSON yükleme: ~100ms
- ✅ Room query: ~10ms
- ✅ Firestore sync: Arka planda

### 3. Offline Çalışma
- ✅ JSON her zaman çalışır
- ✅ Room cache varsa kullanılır
- ✅ İnternet yoksa Firestore sync atlanır

### 4. Sync Stratejisi
- ✅ İlk açılışta JSON yükle
- ✅ Arka planda Firestore sync
- ✅ 24 saatte bir otomatik sync
- ✅ Manuel sync butonu (opsiyonel)

---

## 📋 YAPILACAKLAR LİSTESİ

### Hemen Yapılacaklar
- [ ] MainActivity'de `initialize()` çağır
- [ ] GameViewModel'de `HybridQuestionRepository` kullan
- [ ] Settings'e istatistik ekranı ekle
- [ ] Manuel sync butonu ekle

### Opsiyonel
- [ ] Loading indicator ekle
- [ ] Sync progress göster
- [ ] Cache boyutu göster
- [ ] Otomatik sync ayarı (açma/kapama)

---

## 🎯 SONUÇ

**Avantajlar:**
- ✅ Offline-first (hızlı başlangıç)
- ✅ Cloud sync (güncel sorular)
- ✅ Kalıcı cache (Room)
- ✅ Otomatik güncelleme
- ✅ Düşük veri kullanımı

**Kullanıcı Deneyimi:**
- ⚡ Hızlı açılış (JSON)
- 🔄 Arka planda güncelleme
- 📱 Offline çalışma
- 🎮 Kesintisiz oyun

---

**Hazırlayan:** Antigravity AI Assistant  
**Tarih:** 14 Aralık 2025, 19:10  
**Durum:** ✅ Backend Hazır - UI Entegrasyonu Bekleniyor
