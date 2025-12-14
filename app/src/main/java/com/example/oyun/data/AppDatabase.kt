package com.example.oyun.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.oyun.data.local.CachedQuestion
import com.example.oyun.data.local.CachedQuestionDao

@Database(
    entities = [
        Profile::class,
        HighScore::class,
        CachedQuestion::class  // YENİ: Firestore cache için
    ],
    version = 2,  // Version artırıldı
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao
    abstract fun cachedQuestionDao(): CachedQuestionDao  // YENİ
    
    companion object {
        /**
         * Database migration strategies
         */
        
        /**
         * Version 1 → 2: CachedQuestion tablosu eklendi
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // CachedQuestion tablosunu oluştur
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS cached_questions (
                        id INTEGER PRIMARY KEY NOT NULL,
                        questionText TEXT NOT NULL,
                        optionA TEXT NOT NULL,
                        optionB TEXT NOT NULL,
                        optionC TEXT NOT NULL,
                        optionD TEXT NOT NULL,
                        correctAnswer TEXT NOT NULL,
                        imageName TEXT NOT NULL,
                        level INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        explanation TEXT NOT NULL,
                        tags TEXT NOT NULL,
                        points INTEGER NOT NULL,
                        timeLimit INTEGER NOT NULL,
                        isVerified INTEGER NOT NULL,
                        authorId TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        downloadedAt INTEGER NOT NULL,
                        isFromFirestore INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Index'ler ekle (performans için)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cached_questions_level ON cached_questions(level)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cached_questions_category ON cached_questions(category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cached_questions_difficulty ON cached_questions(difficulty)")
            }
        }
        
        /**
         * Version 2 → 3 örneği (gelecek güncellemeler için hazır)
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Örnek: Yeni tablo veya kolon ekle
                // database.execSQL("ALTER TABLE cached_questions ADD COLUMN newColumn TEXT")
            }
        }
        
        /**
         * Tüm migration'ları döndürür
         */
        fun getAllMigrations(): Array<Migration> {
            return arrayOf(
                MIGRATION_1_2  // Version 1 → 2 migration aktif
                // MIGRATION_2_3  // Gelecek güncellemeler için
            )
        }
    }
}
