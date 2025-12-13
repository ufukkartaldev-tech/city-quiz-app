package com.example.oyun.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Profile::class,
        HighScore::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao
    
    companion object {
        /**
         * Database migration strategies
         * Version 1 → 2 örneği (gelecek güncellemeler için hazır)
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Örnek: Yeni kolon ekle
                // database.execSQL("ALTER TABLE high_scores ADD COLUMN difficulty INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        /**
         * Version 2 → 3 örneği
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Örnek: Yeni tablo oluştur
                // database.execSQL("CREATE TABLE IF NOT EXISTS achievements (...)")
            }
        }
        
        /**
         * Tüm migration'ları döndürür
         */
        fun getAllMigrations(): Array<Migration> {
            return arrayOf(
                // MIGRATION_1_2,  // Version artırıldığında uncomment et
                // MIGRATION_2_3
            )
        }
    }
}
