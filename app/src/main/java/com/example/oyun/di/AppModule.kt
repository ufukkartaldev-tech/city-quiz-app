package com.example.oyun.di

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.oyun.data.AppDatabase
import com.example.oyun.data.HighScoreDao
import com.example.oyun.data.remote.AuthRepository
import com.example.oyun.data.remote.FirebaseSyncService
import com.example.oyun.data.remote.FriendsRepository
import com.example.oyun.data.remote.GoogleSignInHelper
import com.example.oyun.data.remote.HighScoreRemoteRepository
import com.example.oyun.managers.AdManager
import com.example.oyun.managers.SoundManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson {
        return com.google.gson.Gson()
    }

    @Provides
    @Singleton
    fun provideSoundManager(@ApplicationContext context: Context): SoundManager {
        return SoundManager(context)
    }

    @Provides
    @Singleton
    fun provideAdManager(@ApplicationContext context: Context): AdManager {
        return AdManager(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "quiz_db"
        )
            // Migration strategy ekle (gelecek güncellemeler için)
            // .addMigrations(*AppDatabase.getAllMigrations())
            // Geliştirme sırasında fallback kullan (production'da kaldır!)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideHighScoreDao(db: AppDatabase): HighScoreDao {
        return db.highScoreDao()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideHighScoreRemoteRepository(firestore: FirebaseFirestore): HighScoreRemoteRepository {
        return HighScoreRemoteRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideFirebaseSyncService(
        @ApplicationContext context: Context,
        highScoreDao: HighScoreDao,
        remoteRepository: HighScoreRemoteRepository
    ): FirebaseSyncService {
        return FirebaseSyncService(context, highScoreDao, remoteRepository)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, prefs: SharedPreferences): AuthRepository {
        return AuthRepository(auth, prefs)
    }

    @Provides
    @Singleton
    fun provideGoogleSignInHelper(): GoogleSignInHelper {
        return GoogleSignInHelper()
    }

    // ============================================
    // YENİ EKLENDİ: Arkadaş Sistemi
    // ============================================
    @Provides
    @Singleton
    fun provideFriendsRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FriendsRepository {
        return FriendsRepository(firestore, auth)
    }

    // ============================================
    // YENİ EKLENDİ: Bildirim Sistemi
    // ============================================
    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}
