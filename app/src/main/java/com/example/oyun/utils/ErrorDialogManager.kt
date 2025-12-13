package com.example.oyun.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Kullanıcı dostu hata mesajları
 */
sealed class GameError(
    val title: String,
    val message: String,
    val retryAction: (() -> Unit)? = null
) {
    object NoInternet : GameError(
        title = "İnternet Bağlantısı Yok",
        message = "İnternet bağlantınızı kontrol edin ve tekrar deneyin.",
        retryAction = null
    )
    
    object SaveFailed : GameError(
        title = "Kaydetme Hatası",
        message = "Skorunuz kaydedilemedi. Tekrar denemek ister misiniz?",
        retryAction = null
    )
    
    object LoadFailed : GameError(
        title = "Yükleme Hatası",
        message = "Veriler yüklenemedi. Lütfen uygulamayı yeniden başlatın.",
        retryAction = null
    )
    
    object SyncFailed : GameError(
        title = "Senkronizasyon Hatası",
        message = "Skorlarınız senkronize edilemedi. İnternet bağlantınızı kontrol edin.",
        retryAction = null
    )
    
    object QuestionLoadFailed : GameError(
        title = "Soru Yüklenemedi",
        message = "Sorular yüklenirken bir hata oluştu. Lütfen tekrar deneyin.",
        retryAction = null
    )
    
    object AuthFailed : GameError(
        title = "Giriş Hatası",
        message = "Giriş yapılamadı. Lütfen bilgilerinizi kontrol edin.",
        retryAction = null
    )
    
    class Custom(
        title: String,
        message: String,
        retryAction: (() -> Unit)? = null
    ) : GameError(title, message, retryAction)
}

/**
 * Hata dialog'u gösterme yardımcı sınıfı
 */
class ErrorDialogManager(private val context: Context) {
    
    /**
     * Hata dialog'u gösterir
     */
    fun showErrorDialog(
        error: GameError,
        onDismiss: (() -> Unit)? = null
    ) {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(error.title)
            .setMessage(error.message)
            .setIcon(android.R.drawable.ic_dialog_alert)
        
        // Retry butonu varsa ekle
        if (error.retryAction != null) {
            builder.setPositiveButton("Tekrar Dene") { dialog, _ ->
                error.retryAction.invoke()
                dialog.dismiss()
            }
            builder.setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
        } else {
            builder.setPositiveButton("Tamam") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
        }
        
        builder.show()
    }
    
    /**
     * Basit hata mesajı gösterir
     */
    fun showSimpleError(message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Hata")
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Tamam", null)
            .show()
    }
    
    /**
     * Onay dialog'u gösterir
     */
    fun showConfirmDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("Evet") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton("Hayır") { dialog, _ ->
                onCancel?.invoke()
                dialog.dismiss()
            }
            .show()
    }
}
